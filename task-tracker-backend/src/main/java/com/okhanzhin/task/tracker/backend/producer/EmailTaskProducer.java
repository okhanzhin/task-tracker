package com.okhanzhin.task.tracker.backend.producer;

import com.okhanzhin.task.tracker.backend.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTaskProducer {

    private final KafkaTemplate<String, EmailSendingTask> kafkaTemplate;
    private final AppKafkaProperties appKafkaProperties;

    public void send(EmailSendingTask emailSendingTask) {
        String topic =
                appKafkaProperties.getTopics().getEmailSending();

        kafkaTemplate.send(
                topic,
                emailSendingTask.getRecipientEmail(),
                emailSendingTask
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish email task: topic={}, messageId={}",
                        topic,
                        emailSendingTask.getMessageId(),
                        exception
                );

                return;
            }

            var metadata = result.getRecordMetadata();

            log.debug(
                    "Email task published: topic={}, messageId={}, partition={}, offset={}",
                    metadata.topic(),
                    emailSendingTask.getMessageId(),
                    metadata.partition(),
                    metadata.offset()
            );
        });
    }
}
