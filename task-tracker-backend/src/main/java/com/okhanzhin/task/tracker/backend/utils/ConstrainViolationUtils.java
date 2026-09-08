package com.okhanzhin.task.tracker.backend.utils;

import lombok.experimental.UtilityClass;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

/**
 *
 * @author okhanzhin on 31.08.2026
 */
@UtilityClass
public class ConstrainViolationUtils {

    public static Optional<String> findConstraintName(
            DataIntegrityViolationException exception
    ) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof ConstraintViolationException hibernateException) {
                return Optional.ofNullable(hibernateException.getConstraintName());
            }

            cause = cause.getCause();
        }

        return Optional.empty();
    }

    public static boolean hasConstraint(
            DataIntegrityViolationException exception,
            String expectedConstraintName
    ) {
        return findConstraintName(exception)
                .filter(expectedConstraintName::equalsIgnoreCase)
                .isPresent();
    }
}
