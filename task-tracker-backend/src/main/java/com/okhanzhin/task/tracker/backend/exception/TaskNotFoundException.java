package com.okhanzhin.task.tracker.backend.exception;

import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 * Исключение при отсутствии у пользователя задачи
 *
 * @author okhanzhin on 10.09.2026
 */
public class TaskNotFoundException extends ApiException {
    public TaskNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.TASK_NOT_FOUND);
    }
}
