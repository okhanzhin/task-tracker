package com.okhanzhin.task.tracker.backend.mapper;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .status(Status.ACTIVE)
                .build();
    }

    public UserDTO mapToFullDTO(UserDAO userDAO) {
        return UserDTO.builder()
                .id(userDAO.getId())
                .username(userDAO.getUsername())
                .email(userDAO.getEmail())
                .password(userDAO.getPassword())
                .status(userDAO.getStatus())
                .build();
    }

    public UserDTO mapToUserResponse(UserDAO userDAO) {
        return UserDTO.builder()
                .id(userDAO.getId())
                .email(userDAO.getEmail())
                .build();
    }
}
