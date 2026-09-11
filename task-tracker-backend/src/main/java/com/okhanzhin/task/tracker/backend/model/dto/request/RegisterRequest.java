package com.okhanzhin.task.tracker.backend.model.dto.request;

import com.okhanzhin.task.tracker.backend.annotation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на регистрацию нового пользователя
 *
 * @author okhanzhin on 30.08.2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatches
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 255, message = "Username must be between 5 and 255 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Size(max = 254, message = "Email must be less than 255 characters")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;

    @NotBlank(message = "Please repeat the password")
    private String confirmPassword;

    @NotBlank(message = "Time zone must not be blank")
    @Size(max = 64, message = "Time zone must not exceed 64 characters")
    private String timezone;
}
