package com.okhanzhin.task.tracker.backend.exception.error;

/**
 * Сообщения об ошибках
 *
 * @author okhanzhin on 31.08.2026
 */
public enum ErrorMessage {

    USER_NOT_FOUND ("User not found"),
    TASK_NOT_FOUND ("Task not found"),
    EMAIL_ALREADY_REGISTERED("This email is already registered"),
    INVALID_REPORT_PERIOD("Report period start must be before its end"),
    INVALID_REPORT_LIMIT("Report limit must be between 1 and 500"),
    INVALID_REPORT_CURSOR("Report cursor must be positive"),
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
