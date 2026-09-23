package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.email.sender.service.model.EmailSendingTask;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EmailSendingTask> emailSendingTaskProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, EmailSendingTask> emailSendingTaskKafkaTemplate(
            ProducerFactory<String, EmailSendingTask> emailSendingTaskProducerFactory
    ) {
        return new KafkaTemplate<>(emailSendingTaskProducerFactory);
    }
}
