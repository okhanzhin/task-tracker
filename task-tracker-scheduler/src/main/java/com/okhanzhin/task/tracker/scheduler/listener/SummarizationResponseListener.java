package com.okhanzhin.task.tracker.scheduler.listener;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.rpc.PendingSummarizationRequestRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SummarizationResponseListener {

    private final PendingSummarizationRequestRegistry registry;

    @KafkaListener(
            topics = "${app.kafka.topics.summarization-response}"
    )
    public void handle(SummarizationResponse response) {
        boolean completed = registry.complete(response);

        if (completed) {
            log.info(
                    "Completed summarization request: requestId={}",
                    response.getRequestId()
            );
            return;
        }

        log.warn(
                "Late, duplicate or unknown response: requestId={}",
                response.getRequestId()
        );
    }
}
