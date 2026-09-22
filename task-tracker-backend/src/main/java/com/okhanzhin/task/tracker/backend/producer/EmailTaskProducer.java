package com.okhanzhin.task.tracker.backend.producer;

import com.okhanzhin.task.tracker.backend.config.properties.KafkaTopicsProperties;
import com.okhanzhin.task.tracker.backend.model.dto.kafka.EmailSendingTask;
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
    private final KafkaTopicsProperties kafkaTopicsProperties;

    public void send(EmailSendingTask emailSendingTask) {
        kafkaTemplate.send(
                kafkaTopicsProperties.getEmailSending(),
                emailSendingTask.getRecipientEmail(),
                emailSendingTask
        );
    }
}
