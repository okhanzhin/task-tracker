package com.okhanzhin.task.tracker.scheduler.service.impl;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.mapper.ReportEmailMapper;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.scheduler.service.FallbackReportBuilder;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportDeliveryServiceImpl implements UserReportDeliveryService {

    private final UserReportService userReportService;
    private final ReportEmailMapper reportEmailMapper;
    private final EmailTaskProducer emailTaskProducer;
    private final FallbackReportBuilder fallbackReportBuilder;

    @Override
    public CompletableFuture<Void> generateAndEnqueue(SummarizationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        return generateSummary(request)
                .exceptionally(exception -> {
                    Throwable cause = unwrap(exception);

                    if (cause instanceof Error error) {
                        throw error;
                    }

                    log.warn(
                            "Using fallback report: requestId={}, userId={}",
                            request.getRequestId(),
                            request.getUserId(),
                            cause
                    );

                    return fallbackReportBuilder.build(request);
                })
                .thenApply(summary ->
                        reportEmailMapper.toEmailTask(request, summary)
                )
                .thenCompose(emailTaskProducer::send)
                .thenAccept(sendResult -> {});
    }

    private CompletableFuture<String> generateSummary(
            SummarizationRequest request
    ) {
        try {
            return userReportService.generate(request);
        } catch (RuntimeException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private Throwable unwrap(Throwable exception) {
        Throwable cause = exception;

        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause;
    }
}
