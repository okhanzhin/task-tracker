package com.okhanzhin.email.sender.service.service.impl;

import com.okhanzhin.email.sender.service.model.EmailNotification;
import com.okhanzhin.email.sender.service.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 *
 * @author okhanzhin on 16.09.2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public boolean sendEmailNotification(EmailNotification notification) {
        var message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(notification.getRecipientEMail());
        message.setSubject(notification.getSubject());
        message.setText(notification.getText());

        try {
            mailSender.send(message);
            log.info("Notification successfully send to {}", notification.getRecipientEMail());
            return true;
        } catch (MailException e) {
            log.error("Error sending notification to {}: {}", notification.getRecipientEMail(), e.getMessage());
            return false;
        }
    }
}
