package com.okhanzhin.task.tracker.scheduler.config;

import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
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

        ExponentialBackOffWithMaxRetries backoff =
                new ExponentialBackOffWithMaxRetries(retry.getMaxRetries());

        backoff.setInitialInterval(retry.getInitialIntervalMs());
        backoff.setMultiplier(retry.getMultiplier());
        backoff.setMaxInterval(retry.getMaxIntervalMs());

        return new DefaultErrorHandler(
                (consumerRecord, exception) -> log.error(
                        "Kafka record was not processed: "
                                + "topic={}, partition={}, offset={}",
                        consumerRecord.topic(),
                        consumerRecord.partition(),
                        consumerRecord.offset(),
                        exception
                ),
                backoff
        );
    }
}
