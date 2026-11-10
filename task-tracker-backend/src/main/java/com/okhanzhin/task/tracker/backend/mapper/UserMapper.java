package com.okhanzhin.task.tracker.backend.mapper;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
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

    public UserDAO map(RegisterRequest registerRequest) {
        return UserDAO.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(Status.ACTIVE)
                .timezone(registerRequest.getTimezone())
                .build();
    }

    public UserResponse mapToUserResponse(UserDAO userDAO) {
        return UserResponse.builder()
                .id(userDAO.getId())
                .email(userDAO.getEmail())
                .build();
    }

    public SecurityUser mapToUserDetails(UserDAO userDAO) {
        return SecurityUser.builder()
                .userId(userDAO.getId())
                .username(userDAO.getEmail())
                .password(userDAO.getPassword())
                .authorities(List.of())
                .isActive(userDAO.getStatus() == Status.ACTIVE)
                .build();
    }
}
