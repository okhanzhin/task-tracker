package com.okhanzhin.summarization.service.listener;

import com.okhanzhin.summarization.service.config.properties.AppKafkaProperties;
import com.okhanzhin.summarization.service.producer.SummarizationResponseProducer;
import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
public class SummarizationRequestListener {

    private final AppKafkaProperties appKafkaProperties;
    private final SummarizationResponseProducer responseProducer;
    private final SummaryGeneratorService summaryGeneratorService;

    @KafkaListener(
            topics = "${app.kafka.topics.summarization-request-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(SummarizationRequest request) {
        log.info(
                "Received summarization request: requestId={}, userId={}",
                request.getRequestId(),
                request.getUserId()
        );

        String responseTopic = resolveResponseTopic(request);
        SummarizationResponse response = generateResponse(request);

        responseProducer.send(responseTopic, response).join();
    }

    private SummarizationResponse generateResponse(
            SummarizationRequest request
    ) {
        try {
            String summary = summaryGeneratorService.generate(request);

            return SummarizationResponse.builder()
                    .requestId(request.getRequestId())
                    .userId(request.getUserId())
                    .userEmail(request.getUserEmail())
                    .summary(summary)
                    .success(true)
                    .build();
        } catch (Exception exception) {
            log.error(
                    "Failed to generate summary: requestId={}, userId={}",
                    request.getRequestId(),
                    request.getUserId(),
                    exception
            );

            return SummarizationResponse.builder()
                    .requestId(request.getRequestId())
                    .userId(request.getUserId())
                    .userEmail(request.getUserEmail())
                    .success(false)
                    .errorMessage("Failed to generate daily summary")
                    .build();
        }
    }

    private String resolveResponseTopic(SummarizationRequest request) {
        if (StringUtils.hasText(request.getReplyTopic())) {
            return request.getReplyTopic();
        }

        return appKafkaProperties
                .getTopics()
                .getSummarizationResponseTopic();
    }
}
