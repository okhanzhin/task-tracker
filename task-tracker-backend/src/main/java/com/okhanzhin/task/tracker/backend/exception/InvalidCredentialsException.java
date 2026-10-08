package com.okhanzhin.task.tracker.backend.exception;

import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Исключение при ошибке аутентификации в системе
 *
 * @author okhanzhin on 31.08.2026
 */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorMessage.INVALID_CREDENTIAL);
    }
}
