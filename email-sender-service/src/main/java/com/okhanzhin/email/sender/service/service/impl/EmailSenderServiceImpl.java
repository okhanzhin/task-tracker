package com.okhanzhin.email.sender.service.service.impl;

import com.okhanzhin.email.sender.service.config.properties.MailProperties;
import com.okhanzhin.email.sender.service.exception.EmailSendingException;
import com.okhanzhin.email.sender.service.model.EmailNotification;
import com.okhanzhin.email.sender.service.service.EmailSenderService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 *
 * @author okhanzhin on 16.09.2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @Override
    public void send(EmailNotification notification) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(mailProperties.fromAddress());
            helper.setTo(notification.getRecipientEmail());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getBody(), false);

            mailSender.send(message);

            log.info("Email send: messageId={}, recipient={}",
                    notification.getMessageId(),
                    notification.getRecipientEmail());
        } catch (MessagingException | MailException exception) {
            log.error(
                    "Email sending failed: messageId={}, recipient={}",
                    notification.getMessageId(),
                    notification.getRecipientEmail(),
                    exception
            );
            throw new EmailSendingException(notification.getMessageId(), exception);
        }
    }
}
