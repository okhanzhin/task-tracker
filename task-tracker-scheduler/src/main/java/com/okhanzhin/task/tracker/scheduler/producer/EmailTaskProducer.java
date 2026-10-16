package com.okhanzhin.task.tracker.scheduler.producer;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Component
@RequiredArgsConstructor
public class EmailTaskProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppKafkaProperties appKafkaProperties;

    public CompletableFuture<SendResult<String, Object>> send(EmailSendingTask emailSendingTask) {
        Objects.requireNonNull(
                emailSendingTask,
                "emailSendingTask must not be null"
        );

        return kafkaTemplate.send(
                appKafkaProperties.getTopics().getEmailSending(),
                emailSendingTask.getRecipientEmail(),
                emailSendingTask
        );
    }
}
