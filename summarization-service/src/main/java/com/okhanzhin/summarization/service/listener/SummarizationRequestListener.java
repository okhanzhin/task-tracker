package com.okhanzhin.summarization.service.listener;

import com.okhanzhin.summarization.service.config.properties.AppKafkaProperties;
import com.okhanzhin.summarization.service.producer.SummarizationResponseProducer;
import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.request.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.response.SummarizationResponse;
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

        String responseTopic = StringUtils.hasText(request.getReplyTopic())
                ? request.getReplyTopic()
                : appKafkaProperties.getTopics().getSummarizationResponseTopic();

        try {
            String summary = summaryGeneratorService.generate(request);

            SummarizationResponse response = SummarizationResponse.builder()
                    .requestId(request.getRequestId())
                    .userId(request.getUserId())
                    .userEmail(request.getUserEmail())
                    .summary(summary)
                    .success(true)
                    .build();

            responseProducer.send(responseTopic, response);
        } catch (Exception exception) {
            log.error(
                    "Failed to generate summary: requestId={}, userId={}",
                    request.getRequestId(),
                    request.getUserId(),
                    exception
            );

            SummarizationResponse response = SummarizationResponse.builder()
                    .requestId(request.getRequestId())
                    .userId(request.getUserId())
                    .userEmail(request.getUserEmail())
                    .success(false)
                    .errorMessage("Failed to generate daily summary")
                    .build();

            responseProducer.send(responseTopic, response);
        }
    }
}
