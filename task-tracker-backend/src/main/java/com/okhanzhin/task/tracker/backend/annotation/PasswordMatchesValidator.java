package com.okhanzhin.task.tracker.backend.annotation;

import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 *
 * Правило проверки совпадения паролей в запросе на создание пользователя
 *
 * @author okhanzhin on 31.08.2026
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext constraintValidatorContext) {
        return request != null &&
                request.getPassword().equals(request.getConfirmPassword());
    }
}
