package com.okhanzhin.task.tracker.scheduler.producer;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.rpc.PendingSummarizationRequestRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Component
@RequiredArgsConstructor
public class SummarizationRequestProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PendingSummarizationRequestRegistry registry;
    private final AppKafkaProperties kafkaProperties;

    public CompletableFuture<SummarizationResponse> send(SummarizationRequest request) {
        Objects.requireNonNull(
                request,
                "request must not be null"
        );
        Objects.requireNonNull(
                request.getUserId(),
                "userId must not be null"
        );

        String topic = kafkaProperties.getTopics()
                .getSummarizationRequest();
        String key = request.getUserId().toString();

        CompletableFuture<SummarizationResponse> responseFuture = registry.register(request.getRequestId());

        try {
            kafkaTemplate.send(topic, key, request)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            registry.completeExceptionally(
                                    request.getRequestId(),
                                    exception
                            );
                        }
                    });
        } catch (RuntimeException exception) {
            registry.completeExceptionally(
                    request.getRequestId(),
                    exception
            );
        }

        return responseFuture;
    }

}
