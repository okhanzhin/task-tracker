package com.okhanzhin.email.sender.service.listener;

import com.okhanzhin.email.sender.service.exception.InvalidEmailTaskException;
import com.okhanzhin.email.sender.service.mapper.EmailNotificationMapper;
import com.okhanzhin.email.sender.service.service.EmailSenderService;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTaskListener {

    private final EmailSenderService emailSenderService;
    private final EmailNotificationMapper mapper;
    private final Validator validator;

    @KafkaListener(
            topics = "${app.kafka.topics.email-sending}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEmailSendingTask(EmailSendingTask task) {
        validate(task);

        log.info(
                "Received email task: messageId={}, recipientEmail={}, subject={}",
                task.getMessageId(),
                task.getRecipientEmail(),
                task.getSubject()
        );

        emailSenderService.send(
                mapper.mapFromEmailSendingTask(task)
        );
    }

    private void validate(EmailSendingTask task) {
        if (task == null) {
            throw new InvalidEmailTaskException(
                    null,
                    "Email sending task must not be null"
            );
        }

        var violations = validator.validate(task);

        if (violations.isEmpty()) {
            return;
        }

        String details = violations.stream()
                .map(violation -> violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        throw new InvalidEmailTaskException(
                task.getMessageId(),
                details
        );
    }
}
