package com.okhanzhin.summarization.service.config;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
@Configuration(proxyBeanMethods = false)
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, SummarizationResponse> summarizationResponseProducerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, SummarizationResponse> summarizationResponseKafkaTemplate(
            ProducerFactory<String, SummarizationResponse> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
