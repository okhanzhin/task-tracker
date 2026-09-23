package com.okhanzhin.task.tracker.backend.config;

import com.okhanzhin.task.tracker.backend.model.dto.kafka.EmailSendingTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, EmailSendingTask> emailSendingTaskKafkaTemplate(
            ProducerFactory<String, EmailSendingTask> emailSendingTaskProducerFactory
    ) {
        return new KafkaTemplate<>(emailSendingTaskProducerFactory);
    }
}
