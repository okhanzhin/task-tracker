package com.okhanzhin.summarization.service.config;

import com.okhanzhin.summarization.service.config.properties.AppKafkaProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            AppKafkaProperties properties
    ) {
        AppKafkaProperties.Retry retry = properties.getRetry();

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(
                        retry.getMaxRetries()
                );

        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());

        return new DefaultErrorHandler(
                (record, exception) -> log.error(
                        "Kafka record was not processed: "
                                + "topic={}, partition={}, offset={}",
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        exception
                ),
                backOff
        );
    }
}
