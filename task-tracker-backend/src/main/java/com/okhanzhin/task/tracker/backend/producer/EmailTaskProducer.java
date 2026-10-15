package com.okhanzhin.task.tracker.backend.producer;

import com.okhanzhin.task.tracker.backend.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Component
@RequiredArgsConstructor
public class EmailTaskProducer {

    private final KafkaTemplate<String, EmailSendingTask> kafkaTemplate;
    private final AppKafkaProperties appKafkaProperties;

    public void send(EmailSendingTask emailSendingTask) {
        kafkaTemplate.send(
                appKafkaProperties.getTopics().getEmailSending(),
                emailSendingTask.getRecipientEmail(),
                emailSendingTask
        );
    }
}
