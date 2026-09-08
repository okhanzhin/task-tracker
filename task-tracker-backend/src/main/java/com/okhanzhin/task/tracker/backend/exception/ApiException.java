package com.okhanzhin.task.tracker.backend.exception;

import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Базовый класс для всех доменных исключений, выбрасываемых приложением.
 *
 * @author okhanzhin on 30.08.2026
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorMessage errorMessage;

    protected ApiException(HttpStatus status, ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
