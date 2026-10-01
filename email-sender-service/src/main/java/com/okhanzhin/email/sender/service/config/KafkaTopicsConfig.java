package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.email.sender.service.config.properties.AppKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicsConfig {

    private final AppKafkaProperties appKafkaProperties;

    @Bean
    public NewTopic emailSendingTasksTopic() {
        AppKafkaProperties.Topics topics = appKafkaProperties.getTopics();

        return TopicBuilder.name(topics.getEmailSending())
                .partitions(topics.getPartitions())
                .replicas(topics.getReplicas())
                .build();
    }

    @Bean
    public NewTopic emailSendingTasksDtlTopic() {
        AppKafkaProperties.Topics topics = appKafkaProperties.getTopics();

        return TopicBuilder.name(topics.getEmailSendingDlt())
                .partitions(topics.getPartitions())
                .replicas(topics.getReplicas())
                .build();
    }
}
