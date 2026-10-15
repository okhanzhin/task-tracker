package com.okhanzhin.task.tracker.scheduler.rpc;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author okhanzhin on 13.10.2026
 */
@Component
@RequiredArgsConstructor
public class PendingSummarizationRequestRegistry {

    private final AppKafkaProperties kafkaProperties;

    private final ConcurrentMap<String, CompletableFuture<SummarizationResponse>> pendingRequests = new ConcurrentHashMap<>();

    public CompletableFuture<SummarizationResponse> register(String requestId) {
        validateRequestId(requestId);

        CompletableFuture<SummarizationResponse> future = new CompletableFuture<>();

        CompletableFuture<SummarizationResponse> existingFuture = pendingRequests.putIfAbsent(requestId, future);

        if (existingFuture != null) {
            throw new IllegalStateException(
                    "Summarization request is already registered: "
                            + requestId
            );
        }

        configureTimeout(requestId, future);

        return future;
    }

    public boolean complete(SummarizationResponse response) {
        if (response == null) {
            return false;
        }

        CompletableFuture<SummarizationResponse> future = pendingRequests.get(response.getRequestId());

        if (future == null) {
            return false;
        }

        return future.complete(response);
    }

    public boolean completeExceptionally(String requestId, Throwable exception) {
        CompletableFuture<SummarizationResponse> future = pendingRequests.get(requestId);

        if (future == null) {
            return false;
        }

        return future.completeExceptionally(exception);
    }

    public int size() {
        return pendingRequests.size();
    }

    private void configureTimeout(String requestId, CompletableFuture<SummarizationResponse> future) {
        Duration timeout = kafkaProperties.getRpc().getResponseTimeout();

        future
                .orTimeout(
                        timeout.toMillis(),
                        TimeUnit.MILLISECONDS
                )
                .whenComplete((response, exception) ->
                        pendingRequests.remove(requestId, future));
    }

    private void validateRequestId(String requestId) {
        if (!StringUtils.hasText(requestId)) {
            throw new IllegalArgumentException(
                    "Summarization requestId must not be blank"
            );
        }
    }
}
