package com.okhanzhin.task.tracker.scheduler.producer;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.rpc.PendingSummarizationRequestRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
        CompletableFuture<SummarizationResponse> responseFuture = registry.register(request.getRequestId());

        String topic = kafkaProperties.getTopics().getSummarizationRequest();
        String key = request.getUserId().toString();

        kafkaTemplate
                .send(topic, key, request)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        return;
                    }

                    registry.completeExceptionally(request.getRequestId(), exception);
                });

        return responseFuture;
    }

}
