package com.okhanzhin.task.tracker.backend.service;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Интерфейс для работы с пользователями
 *
 * @author okhanzhin on 25.08.2026
 */
public interface UserService {

    SecurityUser createUser(RegisterRequest registerRequest) throws ApiException;

    UserResponse findByUserDetails(UserDetails userDetails) throws ApiException;
}
