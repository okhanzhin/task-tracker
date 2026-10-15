package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.email.sender.service.config.properties.AppKafkaProperties;
import com.okhanzhin.email.sender.service.exception.EmailSendingException;
import com.okhanzhin.email.sender.service.exception.InvalidEmailTaskException;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlingConfig {

    private final AppKafkaProperties appKafkaProperties;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, EmailSendingTask> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    log.error(
                            "Email task moved to DLT: messageId={}, topic={}, partition={}, offset={}, destinationTopic={}",
                            extractMessageId(record.value()),
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            appKafkaProperties.getTopics().getEmailSendingDlt(),
                            exception
                    );

                    return new TopicPartition(
                            appKafkaProperties.getTopics().getEmailSendingDlt(),
                            record.partition()
                    );
                }
        );

        AppKafkaProperties.Retry retry = appKafkaProperties.getRetry();

        ExponentialBackOffWithMaxRetries backOff =
                new ExponentialBackOffWithMaxRetries(retry.getMaxRetries());

        backOff.setInitialInterval(retry.getInitialIntervalMs());
        backOff.setMultiplier(retry.getMultiplier());
        backOff.setMaxInterval(retry.getMaxIntervalMs());

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(InvalidEmailTaskException.class);
        errorHandler.addRetryableExceptions(EmailSendingException.class);

        return errorHandler;
    }

    private String extractMessageId(Object value) {
        if (value instanceof EmailSendingTask task) {
            return String.valueOf(task.getMessageId());
        }

        return "unknown";
    }
}
