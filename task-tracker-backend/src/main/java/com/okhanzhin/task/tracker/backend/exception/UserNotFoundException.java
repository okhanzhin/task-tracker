package com.okhanzhin.task.tracker.backend.exception;

import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Исключение при отсутствии зарегистрированного пользователя
 *
 * @author okhanzhin on 31.08.2026
 */
public class UserNotFoundException extends ApiException{
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.USER_NOT_FOUND);
    }
}
