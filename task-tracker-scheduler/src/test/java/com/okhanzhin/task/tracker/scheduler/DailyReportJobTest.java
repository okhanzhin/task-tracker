package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportJob;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportRequestSource;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@ExtendWith(MockitoExtension.class)
class DailyReportJobTest {

    private static final Instant FROM =
            Instant.parse("2026-10-15T00:00:00Z");

    private static final Instant TO =
            Instant.parse("2026-10-16T00:00:00Z");

    @Mock
    private DailyReportRequestSource requestSource;

    @Mock
    private UserReportDeliveryService deliveryService;

    private DailyReportJob job;

    @BeforeEach
    void setUp() {
        job = new DailyReportJob(
                requestSource,
                deliveryService
        );
    }

    @Test
    void shouldProcessUsersInSourceOrder() {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        provideRequests(firstRequest, secondRequest);

        when(deliveryService.generateAndEnqueue(firstRequest))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(deliveryService.generateAndEnqueue(secondRequest))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.run(FROM, TO);

        InOrder order = inOrder(deliveryService);

        order.verify(deliveryService)
                .generateAndEnqueue(firstRequest);
        order.verify(deliveryService)
                .generateAndEnqueue(secondRequest);
        order.verifyNoMoreInteractions();

        verify(requestSource).forEach(
                eq(FROM),
                eq(TO),
                any()
        );
    }

    @Test
    void shouldContinueWhenUserProcessingFailsAsynchronously() {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        provideRequests(firstRequest, secondRequest);

        when(deliveryService.generateAndEnqueue(firstRequest))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException(
                                        "Report generation failed"
                                )
                        )
                );
        when(deliveryService.generateAndEnqueue(secondRequest))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.run(FROM, TO);

        verify(deliveryService)
                .generateAndEnqueue(firstRequest);
        verify(deliveryService)
                .generateAndEnqueue(secondRequest);
    }

    @Test
    void shouldContinueWhenUserProcessingThrowsSynchronously() {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        provideRequests(firstRequest, secondRequest);

        when(deliveryService.generateAndEnqueue(firstRequest))
                .thenThrow(
                        new IllegalStateException(
                                "Report generation could not be started"
                        )
                );
        when(deliveryService.generateAndEnqueue(secondRequest))
                .thenReturn(CompletableFuture.completedFuture(null));

        job.run(FROM, TO);

        verify(deliveryService)
                .generateAndEnqueue(secondRequest);
    }

    @Test
    void shouldPropagateSourceFailure() {
        RuntimeException failure =
                new RuntimeException("Backend request failed");

        doThrow(failure)
                .when(requestSource)
                .forEach(eq(FROM), eq(TO), any());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> job.run(FROM, TO)
        );

        assertSame(failure, exception);
        verifyNoInteractions(deliveryService);
    }

    @Test
    void shouldDoNothingWhenSourceHasNoRequests() {
        job.run(FROM, TO);

        verify(requestSource).forEach(
                eq(FROM),
                eq(TO),
                any()
        );
        verifyNoInteractions(deliveryService);
    }

    @Test
    void shouldRejectEqualPeriodBoundaries() {
        assertThrows(
                IllegalArgumentException.class,
                () -> job.run(FROM, FROM)
        );

        verifyNoInteractions(requestSource, deliveryService);
    }

    @Test
    void shouldRejectReversedPeriod() {
        assertThrows(
                IllegalArgumentException.class,
                () -> job.run(TO, FROM)
        );

        verifyNoInteractions(requestSource, deliveryService);
    }

    @Test
    void shouldRejectNullPeriodBoundary() {
        assertThrows(
                NullPointerException.class,
                () -> job.run(null, TO)
        );

        assertThrows(
                NullPointerException.class,
                () -> job.run(FROM, null)
        );

        verifyNoInteractions(requestSource, deliveryService);
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

    private SummarizationRequest createRequest(Long userId) {
        return SummarizationRequest.builder()
                .requestId("report-request-" + userId)
                .userId(userId)
                .userEmail("user" + userId + "@example.com")
                .build();
    }
}
