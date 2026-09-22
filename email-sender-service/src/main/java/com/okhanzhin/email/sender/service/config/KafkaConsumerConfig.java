package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.email.sender.service.model.EmailSendingTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EmailSendingTask> emailSendingTaskConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = kafkaProperties.buildConsumerProperties();

        JsonDeserializer<EmailSendingTask> valueDeserializer =
                new JsonDeserializer<>(EmailSendingTask.class, false);

        valueDeserializer.addTrustedPackages(
                "com.okhanzhin.email.sender.service.model"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailSendingTask> kafkaListenerContainerFactory(
            ConsumerFactory<String, EmailSendingTask> emailSendingTaskConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EmailSendingTask> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(emailSendingTaskConsumerFactory);

        return factory;
    }
}
