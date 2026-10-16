package com.okhanzhin.email.sender.service.mapper;

import com.okhanzhin.email.sender.service.model.EmailNotification;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import org.springframework.stereotype.Component;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Component
public class EmailNotificationMapper {

    public EmailNotification mapFromEmailSendingTask(EmailSendingTask task) {

        return EmailNotification.builder()
                .messageId(task.getMessageId())
                .recipientEmail(task.getRecipientEmail())
                .subject(task.getSubject())
                .body(task.getBody())
                .build();
    }
}
