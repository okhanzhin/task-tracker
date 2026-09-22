package com.okhanzhin.email.sender.service.listener;

import com.okhanzhin.email.sender.service.mapper.EmailNotificationMapper;
import com.okhanzhin.email.sender.service.model.EmailSendingTask;
import com.okhanzhin.email.sender.service.service.EmailSenderService;
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

    @KafkaListener(
            topics = "${app.kafka.topics.email-sending}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEmailSendingTask(EmailSendingTask task) {
        log.info(
                "Received email task: recipientEmail={}, subject={}",
                task.getRecipientEmail(),
                task.getBody()
        );

        emailSenderService.sendEmailNotification(mapper.mapFromEmailSendingTask(task));
    }
}
