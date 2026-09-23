package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.task.tracker.common.model.EmailSendingTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EmailSendingTask> kafkaListenerContainerFactory(
            ConsumerFactory<String, EmailSendingTask> emailSendingTaskConsumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EmailSendingTask> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(emailSendingTaskConsumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}
