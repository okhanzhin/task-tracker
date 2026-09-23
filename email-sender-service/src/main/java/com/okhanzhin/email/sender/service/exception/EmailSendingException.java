package com.okhanzhin.email.sender.service.exception;

import java.util.UUID;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
public class EmailSendingException extends RuntimeException{

    private final UUID messageId;

    public EmailSendingException(UUID messageId, Throwable cause) {
        super("Failed to send email with messageId=" + messageId, cause);
        this.messageId = messageId;
    }

    public UUID getMessageId() {
        return messageId;
    }
}
