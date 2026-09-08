package com.okhanzhin.task.tracker.backend.exception.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

/**
 * Ответ с ошибкой
 *
 * @author okhanzhin on 31.08.2026
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(String message, List<String> errors) {

    public ErrorResponse(String message) {
        this(message, Collections.emptyList());
    }
}
