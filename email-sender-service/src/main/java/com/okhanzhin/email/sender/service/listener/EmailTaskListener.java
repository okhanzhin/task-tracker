package com.okhanzhin.email.sender.service.listener;

import com.okhanzhin.email.sender.service.mapper.EmailNotificationMapper;
import com.okhanzhin.email.sender.service.service.EmailSenderService;
import com.okhanzhin.email.sender.service.utils.EmailSendingTaskValidator;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
    private final EmailSendingTaskValidator validator;

    @KafkaListener(
            topics = "${app.kafka.topics.email-sending}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEmailSendingTask(EmailSendingTask task) {
        validator.validate(task);

        log.info(
                "Received email task: messageId={}, recipientEmail={}, subject={}",
                task.getMessageId(),
                task.getRecipientEmail(),
                task.getSubject()
        );

        emailSenderService.send(mapper.mapFromEmailSendingTask(task));
    }
}
