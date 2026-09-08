package com.okhanzhin.task.tracker.backend.exception;

import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Исключение при попытке регистрации почтового адреса, существующего в системе
 *
 * @author okhanzhin on 30.08.2026
 */
public class EmailAlreadyRegisteredException extends ApiException {

    public EmailAlreadyRegisteredException() {
        super(HttpStatus.CONFLICT, ErrorMessage.EMAIL_ALREADY_REGISTERED);
    }
}
