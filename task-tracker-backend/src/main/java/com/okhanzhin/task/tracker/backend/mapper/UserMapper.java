package com.okhanzhin.task.tracker.backend.mapper;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.dto.SecurityUser;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

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
                .timezone(registerRequest.getTimezone())
                .build();
    }

    public UserDTO mapToFullDTO(UserDAO userDAO) {
        return UserDTO.builder()
                .id(userDAO.getId())
                .username(userDAO.getUsername())
                .email(userDAO.getEmail())
                .password(userDAO.getPassword())
                .status(userDAO.getStatus())
                .timezone(userDAO.getTimezone())
                .build();
    }

    public UserResponse mapToUserResponse(UserDAO userDAO) {
        return UserResponse.builder()
                .id(userDAO.getId())
                .email(userDAO.getEmail())
                .build();
    }

    public UserDetails mapToUserDetails(UserDTO userDTO) {
        return SecurityUser.builder()
                .userId(userDTO.getId())
                .username(userDTO.getEmail())
                .password(userDTO.getPassword())
                .authorities(List.of())
                .isActive(userDTO.getStatus().equals(Status.ACTIVE))
                .build();
    }
}
