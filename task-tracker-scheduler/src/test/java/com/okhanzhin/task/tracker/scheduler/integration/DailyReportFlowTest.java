package com.okhanzhin.task.tracker.scheduler.integration;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.mapper.ReportEmailMapper;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportJob;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportRequestSource;
import com.okhanzhin.task.tracker.scheduler.service.FallbackReportBuilder;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import com.okhanzhin.task.tracker.scheduler.service.impl.UserReportDeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Timeout(5)
@ExtendWith(MockitoExtension.class)
class DailyReportFlowTest {

    private static final Instant FROM =
            Instant.parse("2026-10-15T00:00:00Z");

    private static final Instant TO =
            Instant.parse("2026-10-16T00:00:00Z");

    private static final String EMAIL_TOPIC =
            "EMAIL_SENDING_TASKS";

    private static final String EXPECTED_SUBJECT =
            "Task report for period "
                    + "15.10.2026 00:00 to 16.10.2026 00:00 (UTC)";

    private static final String EXPECTED_EMPTY_FALLBACK =
            "Daily task report\n\n"
                    + "Completed tasks: 0\n"
                    + "No tasks.\n"
                    + "\n"
                    + "Pending tasks: 0\n"
                    + "No tasks.\n"
                    + "\n"
                    + "AI-generated analysis is temporarily unavailable. "
                    + "This report was generated directly from your task data.\n";

    @Mock
    private DailyReportRequestSource requestSource;

    @Mock
    private UserReportService userReportService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private AppKafkaProperties kafkaProperties;

    private DailyReportJob job;

    @BeforeEach
    void setUp() {
        when(kafkaProperties.getTopics().getEmailSending())
                .thenReturn(EMAIL_TOPIC);

        ReportEmailMapper emailMapper =
                new ReportEmailMapper("UTC");

        EmailTaskProducer emailProducer =
                new EmailTaskProducer(
                        kafkaTemplate,
                        kafkaProperties
                );

        UserReportDeliveryService deliveryService =
                new UserReportDeliveryServiceImpl(
                        userReportService,
                        emailMapper,
                        emailProducer,
                        new FallbackReportBuilder()
                );

        job = new DailyReportJob(
                requestSource,
                deliveryService
        );
    }

    @Test
    void shouldPublishEmailWithGeneratedSummary() {
        SummarizationRequest request = createRequest(1L);
        String summary =
                "Completed 3 tasks; 2 tasks remain incomplete.";

        provideRequests(request);

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.completedFuture(summary)
                );

        when(kafkaTemplate.send(
                eq(EMAIL_TOPIC),
                eq(request.getUserEmail()),
                any(EmailSendingTask.class)
        )).thenReturn(successfulPublication());

        job.run(FROM, TO);

        ArgumentCaptor<EmailSendingTask> emailCaptor =
                ArgumentCaptor.forClass(EmailSendingTask.class);

        verify(kafkaTemplate).send(
                eq(EMAIL_TOPIC),
                eq(request.getUserEmail()),
                emailCaptor.capture()
        );

        EmailSendingTask email = emailCaptor.getValue();

        assertEmail(email, request, summary);

        verify(requestSource).forEach(
                eq(FROM),
                eq(TO),
                any()
        );
        verify(userReportService).generate(request);
    }

    @Test
    void shouldPublishFallbackAndContinueAfterGenerationFailure() {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        provideRequests(firstRequest, secondRequest);

        when(userReportService.generate(firstRequest))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new IllegalStateException(
                                        "Summarization service failed"
                                )
                        )
                );

        when(userReportService.generate(secondRequest))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                "Second user report"
                        )
                );

        when(kafkaTemplate.send(
                eq(EMAIL_TOPIC),
                eq(firstRequest.getUserEmail()),
                any(EmailSendingTask.class)
        )).thenReturn(successfulPublication());

        when(kafkaTemplate.send(
                eq(EMAIL_TOPIC),
                eq(secondRequest.getUserEmail()),
                any(EmailSendingTask.class)
        )).thenReturn(successfulPublication());

        assertDoesNotThrow(() -> job.run(FROM, TO));

        ArgumentCaptor<EmailSendingTask> emailCaptor =
                ArgumentCaptor.forClass(EmailSendingTask.class);

        verify(kafkaTemplate, times(2)).send(
                eq(EMAIL_TOPIC),
                any(String.class),
                emailCaptor.capture()
        );

        List<EmailSendingTask> emails =
                emailCaptor.getAllValues();

        assertEmail(
                emails.get(0),
                firstRequest,
                EXPECTED_EMPTY_FALLBACK
        );

        assertEmail(
                emails.get(1),
                secondRequest,
                "Second user report"
        );

        assertNotEquals(
                emails.get(0).getMessageId(),
                emails.get(1).getMessageId()
        );

        verify(userReportService).generate(firstRequest);
        verify(userReportService).generate(secondRequest);
    }

    @Test
    void shouldContinueAfterEmailPublicationFailure() {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        provideRequests(firstRequest, secondRequest);

        when(userReportService.generate(firstRequest))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                "First user report"
                        )
                );

        when(userReportService.generate(secondRequest))
                .thenReturn(
                        CompletableFuture.completedFuture(
                                "Second user report"
                        )
                );

        when(kafkaTemplate.send(
                eq(EMAIL_TOPIC),
                eq(firstRequest.getUserEmail()),
                any(EmailSendingTask.class)
        )).thenReturn(
                CompletableFuture.failedFuture(
                        new IllegalStateException(
                                "Kafka publication failed"
                        )
                )
        );

        when(kafkaTemplate.send(
                eq(EMAIL_TOPIC),
                eq(secondRequest.getUserEmail()),
                any(EmailSendingTask.class)
        )).thenReturn(successfulPublication());

        assertDoesNotThrow(() -> job.run(FROM, TO));

        ArgumentCaptor<EmailSendingTask> emailCaptor =
                ArgumentCaptor.forClass(EmailSendingTask.class);

        verify(kafkaTemplate, times(2)).send(
                eq(EMAIL_TOPIC),
                any(String.class),
                emailCaptor.capture()
        );

        List<EmailSendingTask> emails =
                emailCaptor.getAllValues();

        assertEmail(
                emails.get(0),
                firstRequest,
                "First user report"
        );

        assertEmail(
                emails.get(1),
                secondRequest,
                "Second user report"
        );

        assertNotEquals(
                emails.get(0).getMessageId(),
                emails.get(1).getMessageId()
        );

        verify(userReportService).generate(firstRequest);
        verify(userReportService).generate(secondRequest);
    }

    private void assertEmail(
            EmailSendingTask email,
            SummarizationRequest request,
            String expectedBody
    ) {
        assertEquals(
                request.getUserEmail(),
                email.getRecipientEmail()
        );
        assertEquals(EXPECTED_SUBJECT, email.getSubject());
        assertEquals(expectedBody, email.getBody());
        assertNotNull(email.getMessageId());
    }

    private void provideRequests(
            SummarizationRequest... requests
    ) {
        doAnswer(invocation -> {
            Consumer<SummarizationRequest> consumer =
                    invocation.getArgument(2);

            for (SummarizationRequest request : requests) {
                consumer.accept(request);
            }

            return null;
        }).when(requestSource).forEach(
                eq(FROM),
                eq(TO),
                any()
        );
    }

    private CompletableFuture<SendResult<String, Object>>
    successfulPublication() {
        return CompletableFuture.completedFuture(null);
    }

    private SummarizationRequest createRequest(Long userId) {
        return SummarizationRequest.builder()
                .requestId("report-request-" + userId)
                .userId(userId)
                .userEmail("user" + userId + "@example.com")
                .periodStart(
                        LocalDateTime.parse("2026-10-15T00:00:00")
                )
                .periodEnd(
                        LocalDateTime.parse("2026-10-16T00:00:00")
                )
                .replyTopic("SUMMARIZATION_RESPONSES")
                .tasks(List.of())
                .build();
    }
}
