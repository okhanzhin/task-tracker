package com.okhanzhin.summarization.service.config;

import com.okhanzhin.summarization.service.config.properties.AppKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {

    private final AppKafkaProperties appKafkaProperties;

    @Bean
    public NewTopic summarizationRequestTopic() {
        AppKafkaProperties.Topics topics = appKafkaProperties.getTopics();

        return TopicBuilder.name(topics.getSummarizationRequestTopic())
                .partitions(topics.getPartitions())
                .replicas(topics.getReplicas())
                .build();
    }

    @Bean
    public NewTopic summarizationResponseTopic() {
        AppKafkaProperties.Topics topics = appKafkaProperties.getTopics();

        return TopicBuilder.name(topics.getSummarizationResponseTopic())
                .partitions(topics.getPartitions())
                .replicas(topics.getReplicas())
                .build();
    }
}
