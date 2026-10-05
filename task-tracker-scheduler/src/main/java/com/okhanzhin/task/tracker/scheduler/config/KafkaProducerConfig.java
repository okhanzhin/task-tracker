package com.okhanzhin.task.tracker.scheduler.config;

import com.okhanzhin.task.tracker.common.model.request.SummarizationRequest;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 *
 * @author okhanzhin on 05.10.2026
 */
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, SummarizationRequest> summarizationRequestProducerFactory(
            KafkaProperties kafkaProperties
    ) {
        Map<String, Object> config = kafkaProperties.buildProducerProperties();

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, SummarizationRequest> summarizationRequestKafkaTemplate(
            ProducerFactory<String, SummarizationRequest> summarizationRequestProducerFactory
    ) {
        return new KafkaTemplate<>(summarizationRequestProducerFactory);
    }
}
