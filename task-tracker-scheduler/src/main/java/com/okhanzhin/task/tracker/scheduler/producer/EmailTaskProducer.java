package com.okhanzhin.task.tracker.scheduler.producer;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Component
@RequiredArgsConstructor
public class EmailTaskProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppKafkaProperties appKafkaProperties;

    public void send(EmailSendingTask emailSendingTask) {
        kafkaTemplate.send(
                appKafkaProperties.getTopics().getEmailSending(),
                emailSendingTask.getRecipientEmail(),
                emailSendingTask
        );
    }
}
