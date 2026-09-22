package com.okhanzhin.task.tracker.backend.config;

import com.okhanzhin.task.tracker.backend.config.properties.KafkaTopicsProperties;
import com.okhanzhin.task.tracker.backend.model.dto.kafka.EmailSendingTask;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Configuration
@EnableConfigurationProperties(value = KafkaTopicsProperties.class)
public class KafkaProducerConfiguration {

    @Bean
    public ProducerFactory<String, EmailSendingTask> emailSendingTaskProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties(null);

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, EmailSendingTask> emailSendingTaskKafkaTemplate(
            ProducerFactory<String, EmailSendingTask> emailSendingTaskProducerFactory
    ) {
        return new KafkaTemplate<>(emailSendingTaskProducerFactory);
    }
}
