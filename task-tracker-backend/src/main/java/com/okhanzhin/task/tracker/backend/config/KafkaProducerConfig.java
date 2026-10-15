package com.okhanzhin.task.tracker.backend.config;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Configuration(proxyBeanMethods = false)
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EmailSendingTask> emailSendingProducerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, EmailSendingTask> emailSendingTaskKafkaTemplate(
            ProducerFactory<String, EmailSendingTask> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
