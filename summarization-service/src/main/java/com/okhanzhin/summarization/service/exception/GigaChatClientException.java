package com.okhanzhin.summarization.service.exception;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
public class GigaChatClientException extends RuntimeException {

    public GigaChatClientException(String message) {
        super(message);
    }

    public GigaChatClientException(String message, Throwable cause) {
        super(message, cause);
    }
}