package com.okhanzhin.task.tracker.backend.exception.error;

/**
 * Сообщения об ошибках
 *
 * @author okhanzhin on 31.08.2026
 */
public enum ErrorMessage {

    USER_NOT_FOUND ("User not found"),
    EMAIL_ALREADY_REGISTERED("This email is already registered"),
    VALIDATION_FAILED("Validation failed"),
    INVALID_CREDENTIAL("Invalid email or password"),
    AUTHENTICATION_FAILED("Authentication is required"),
    INTERNAL_ERROR("Internal server error");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
