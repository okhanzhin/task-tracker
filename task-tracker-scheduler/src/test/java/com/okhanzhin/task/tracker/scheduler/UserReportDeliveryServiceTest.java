package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.mapper.ReportEmailMapper;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.scheduler.service.FallbackReportBuilder;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import com.okhanzhin.task.tracker.scheduler.service.impl.UserReportDeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

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
class UserReportDeliveryServiceTest {

    private static final String GENERATED_SUMMARY = "Report summary";
    private static final String FALLBACK_SUMMARY = "Fallback report";

    @Mock
    private UserReportService userReportService;

    @Mock
    private ReportEmailMapper reportEmailMapper;

    @Mock
    private EmailTaskProducer emailTaskProducer;

    @Mock
    private FallbackReportBuilder fallbackReportBuilder;

    private UserReportDeliveryService service;

    @BeforeEach
    void setUp() {
        service = new UserReportDeliveryServiceImpl(
                userReportService,
                reportEmailMapper,
                emailTaskProducer,
                fallbackReportBuilder
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
        when(reportEmailMapper.toEmailTask(request, GENERATED_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(publicationFuture);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        assertFalse(result.isDone());
        verifyNoInteractions(
                reportEmailMapper,
                emailTaskProducer,
                fallbackReportBuilder
        );

        generationFuture.complete(GENERATED_SUMMARY);

        verify(reportEmailMapper).toEmailTask(
                request,
                GENERATED_SUMMARY
        );
        verify(emailTaskProducer).send(emailTask);
        assertFalse(result.isDone());
        verifyNoInteractions(fallbackReportBuilder);

        publicationFuture.complete(null);

        assertNull(result.join());
    }

    @Test
    void shouldPublishFallbackWhenGenerationFails() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        RuntimeException failure =
                new RuntimeException("Summarization failed");

        when(userReportService.generate(request))
                .thenReturn(CompletableFuture.failedFuture(failure));
        when(fallbackReportBuilder.build(request))
                .thenReturn(FALLBACK_SUMMARY);
        when(reportEmailMapper.toEmailTask(request, FALLBACK_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        assertNull(result.join());

        verify(fallbackReportBuilder).build(request);
        verify(reportEmailMapper).toEmailTask(
                request,
                FALLBACK_SUMMARY
        );
        verify(emailTaskProducer).send(emailTask);
    }

    @Test
    void shouldPublishFallbackWhenGenerationThrowsSynchronously() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        RuntimeException failure =
                new RuntimeException("Summarization failed");

        when(userReportService.generate(request))
                .thenThrow(failure);
        when(fallbackReportBuilder.build(request))
                .thenReturn(FALLBACK_SUMMARY);
        when(reportEmailMapper.toEmailTask(request, FALLBACK_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        assertNull(result.join());

        verify(fallbackReportBuilder).build(request);
        verify(reportEmailMapper).toEmailTask(
                request,
                FALLBACK_SUMMARY
        );
        verify(emailTaskProducer).send(emailTask);
    }

    @Test
    void shouldPublishFallbackWhenGenerationTimesOut() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        TimeoutException failure =
                new TimeoutException("Summarization timed out");

        when(userReportService.generate(request))
                .thenReturn(CompletableFuture.failedFuture(failure));
        when(fallbackReportBuilder.build(request))
                .thenReturn(FALLBACK_SUMMARY);
        when(reportEmailMapper.toEmailTask(request, FALLBACK_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        assertNull(result.join());

        verify(fallbackReportBuilder).build(request);
        verify(emailTaskProducer).send(emailTask);
    }

    @Test
    void shouldWaitForFallbackPublication() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        CompletableFuture<SendResult<String, Object>> publicationFuture =
                new CompletableFuture<>();

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException("Summarization failed")
                        )
                );
        when(fallbackReportBuilder.build(request))
                .thenReturn(FALLBACK_SUMMARY);
        when(reportEmailMapper.toEmailTask(request, FALLBACK_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(publicationFuture);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        verify(emailTaskProducer).send(emailTask);
        assertFalse(result.isDone());

        publicationFuture.complete(null);

        assertNull(result.join());
    }

    @Test
    void shouldPropagatePublicationFailureWithoutBuildingFallback() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        RuntimeException failure =
                new RuntimeException("Kafka publication failed");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.completedFuture(GENERATED_SUMMARY)
                );
        when(reportEmailMapper.toEmailTask(request, GENERATED_SUMMARY))
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
        verifyNoInteractions(fallbackReportBuilder);
    }

    @Test
    void shouldPropagateFallbackPublicationFailure() {
        SummarizationRequest request = createRequest();
        EmailSendingTask emailTask = createEmailTask();

        RuntimeException publicationFailure =
                new RuntimeException("Kafka publication failed");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException("Summarization failed")
                        )
                );
        when(fallbackReportBuilder.build(request))
                .thenReturn(FALLBACK_SUMMARY);
        when(reportEmailMapper.toEmailTask(request, FALLBACK_SUMMARY))
                .thenReturn(emailTask);
        when(emailTaskProducer.send(emailTask))
                .thenReturn(
                        CompletableFuture.failedFuture(publicationFailure)
                );

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(publicationFailure, exception.getCause());
        verify(fallbackReportBuilder).build(request);
        verify(emailTaskProducer).send(emailTask);
    }

    @Test
    void shouldNotPublishWhenMappingFails() {
        SummarizationRequest request = createRequest();

        IllegalArgumentException failure =
                new IllegalArgumentException("Invalid email task");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.completedFuture(GENERATED_SUMMARY)
                );
        when(reportEmailMapper.toEmailTask(request, GENERATED_SUMMARY))
                .thenThrow(failure);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(failure, exception.getCause());
        verifyNoInteractions(
                emailTaskProducer,
                fallbackReportBuilder
        );
    }

    @Test
    void shouldNotPublishWhenFallbackBuildingFails() {
        SummarizationRequest request = createRequest();

        RuntimeException generationFailure =
                new RuntimeException("Summarization failed");
        IllegalArgumentException fallbackFailure =
                new IllegalArgumentException("Invalid fallback data");

        when(userReportService.generate(request))
                .thenReturn(
                        CompletableFuture.failedFuture(generationFailure)
                );
        when(fallbackReportBuilder.build(request))
                .thenThrow(fallbackFailure);

        CompletableFuture<Void> result =
                service.generateAndEnqueue(request);

        CompletionException exception = assertThrows(
                CompletionException.class,
                result::join
        );

        assertSame(fallbackFailure, exception.getCause());
        verifyNoInteractions(reportEmailMapper, emailTaskProducer);
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
                .messageId(UUID.randomUUID())
                .recipientEmail("user@example.com")
                .subject("Task report")
                .body(GENERATED_SUMMARY)
                .build();
    }
}