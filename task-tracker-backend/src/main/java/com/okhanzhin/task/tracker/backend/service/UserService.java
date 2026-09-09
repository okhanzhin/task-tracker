package com.okhanzhin.task.tracker.backend.service;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Интерфейс для работы с пользователями
 *
 * @author okhanzhin on 25.08.2026
 */
public interface UserService {

    UserDTO createUser(RegisterRequest registerRequest) throws ApiException;

    UserDTO findByEmail(String email) throws ApiException;

    UserDTO getUser(UserDetails userDetails) throws ApiException;
}
