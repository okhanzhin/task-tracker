package com.okhanzhin.email.sender.service.exception;

import java.util.UUID;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
public class InvalidEmailTaskException extends RuntimeException{

    private final UUID messageId;

    public InvalidEmailTaskException(UUID messageId, String message) {
        super(message);
        this.messageId = messageId;
    }

    public InvalidEmailTaskException(UUID messageId, String message, Throwable cause) {
        super(message, cause);
        this.messageId = messageId;
    }

    public UUID getMessageId() {
        return messageId;
    }
}
