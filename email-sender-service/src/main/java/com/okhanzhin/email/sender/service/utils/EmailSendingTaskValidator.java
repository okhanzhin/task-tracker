package com.okhanzhin.email.sender.service.utils;

import com.okhanzhin.email.sender.service.exception.InvalidEmailTaskException;
import com.okhanzhin.email.sender.service.model.EmailSendingTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
@Component
public class EmailSendingTaskValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
    );

    public void validate(EmailSendingTask task) {
        if (task == null) {
            throw new InvalidEmailTaskException(
                    null,
                    "Email sending task must not be null"
            );
        }

        if (task.getMessageId() == null) {
            throw new InvalidEmailTaskException(
                    null,
                    "messageId must not be null"
            );
        }

        if (!StringUtils.hasText(task.getRecipientEmail())) {
            throw new InvalidEmailTaskException(
                    task.getMessageId(),
                    "recipientEmail must not be blank"
            );
        }

        if (!EMAIL_PATTERN.matcher(task.getRecipientEmail()).matches()) {
            throw new InvalidEmailTaskException(
                    task.getMessageId(),
                    "recipientEmail has invalid format"
            );
        }

        if (!StringUtils.hasText(task.getSubject())) {
            throw new InvalidEmailTaskException(
                    task.getMessageId(),
                    "subject must not be blank"
            );
        }

        if (!StringUtils.hasText(task.getBody())) {
            throw new InvalidEmailTaskException(
                    task.getMessageId(),
                    "body must not be blank"
            );
        }
    }
}
