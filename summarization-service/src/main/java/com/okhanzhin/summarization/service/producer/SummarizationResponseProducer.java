package com.okhanzhin.summarization.service.producer;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.kafka.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SummarizationResponseProducer {

    private final KafkaTemplate<String, SummarizationResponse> kafkaTemplate;

    public CompletableFuture<SendResult<String, SummarizationResponse>> send(
            String topic,
            SummarizationResponse response
    ) {
        return kafkaTemplate.send(topic, response.getRequestId(), response)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to send summarization response: requestId={}, topic={}",
                                response.getRequestId(),
                                topic,
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Summarization response sent: requestId={}, topic={}, partition={}, offset={}",
                            response.getRequestId(),
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );
                });
    }
}
