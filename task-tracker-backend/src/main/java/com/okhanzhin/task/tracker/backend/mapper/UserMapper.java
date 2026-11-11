package com.okhanzhin.task.tracker.backend.mapper;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.model.entity.User;
import com.okhanzhin.task.tracker.backend.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Маппер для пользователей
 *
 * @author okhanzhin on 31.08.2026
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User map(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(Status.ACTIVE)
                .timezone(registerRequest.getTimezone())
                .build();
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    public SecurityUser mapToUserDetails(User user) {
        return SecurityUser.builder()
                .userId(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of())
                .isActive(user.getStatus() == Status.ACTIVE)
                .build();
    }
}
