package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.email.sender.service.config.properties.KafkaTopicsProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
@RequiredArgsConstructor
public class KafkaTopicsConfiguration {

    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Bean
    public NewTopic emailSendingTasksTopic() {
        return TopicBuilder.name(kafkaTopicsProperties.getEmailSending())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
