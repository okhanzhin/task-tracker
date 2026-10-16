package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.mapper.ReportEmailMapper;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import com.okhanzhin.task.tracker.scheduler.service.impl.UserReportDeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
/**
 *
 * @author okhanzhin on 16.10.2026
 */
@ExtendWith(MockitoExtension.class)
public class UserReportDeliveryServiceTest {

    @Mock
    private UserReportService userReportService;

    @Mock
    private ReportEmailMapper reportEmailMapper;

    @Mock
    private EmailTaskProducer emailTaskProducer;

    private UserReportDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new UserReportDeliveryServiceImpl(
                userReportService,
                reportEmailMapper,
                emailTaskProducer
        );
    }

    @Test
    void shouldWaitForGenerationAndPublication() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        CompletableFuture<String> generationFuture =
                new CompletableFuture<>();

        CompletableFuture<SendResult<String, Object>> publicationFuture =
                new CompletableFuture<>();

        when(userReportService.generate(request))
                .thenReturn(generationFuture);
        when(reportEmailMapper.toEmailTask(request, "Report summary"))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(publicationFuture);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        assertFalse(result.isDone());
        verifyNoInteractions(reportEmailMapper, emailTaskProducer);

        generationFuture.complete("Report summary");

        verify(emailTaskProducer).send(emailTask);
        assertFalse(result.isDone());

        publicationFuture.complete(null);

        assertNull(result.join());
    }

    @Test
    void shouldNotPublishWhenGenerationFails() {
        SummarizationRequest request = createRequest();
        RuntimeException failure =
                new RuntimeException("Summarization failed");

        when(userReportService.generate(request))
                .thenReturn(CompletableFuture.failedFuture(failure));

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(failure, exception.getCause());
        verifyNoInteractions(reportEmailMapper, emailTaskProducer);
    }

    @Test
    void shouldPropagatePublicationFailure() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();
        RuntimeException failure =
                new RuntimeException("Kafka publication failed");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.completedFuture("Report summary")
                );
        when(reportEmailMapper.toEmailTask(request, "Report summary"))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(CompletableFuture.failedFuture(failure));

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(failure, exception.getCause());
    }

    @Test
    void shouldNotPublishWhenMappingFails() {
        SummarizationRequest request = createRequest();
        IllegalArgumentException failure =
                new IllegalArgumentException("Invalid email task");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.completedFuture("Report summary")
                );
        when(reportEmailMapper.toEmailTask(request, "Report summary"))
                .thenThrow(failure);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(failure, exception.getCause());
        verifyNoInteractions(emailTaskProducer);
    }

    private SummarizationRequest createRequest() {
        return SummarizationRequest.builder()
                .requestId("report-request-1")
                .userId(1L)
                .userEmail("user@example.com")
                .build();
    }

    private EmailSendingTask createEmailTask() {
        return EmailSendingTask.builder()
                .recipientEmail("user@example.com")
                .subject("Task report")
                .body("Report summary")
                .build();
    }
}
