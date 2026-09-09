package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.exception.InvalidCredentialsException;
import com.okhanzhin.task.tracker.backend.model.dto.SecurityUser;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.AuthenticationRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.AuthenticationResponse;
import com.okhanzhin.task.tracker.backend.service.AuthenticationService;
import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            log.info("User with email: {} successfully authenticated", request.getEmail());
        } catch (AuthenticationException e) {
            log.info("User with email: {} - not authenticated: {}", request.getEmail(), e.getClass().getSimpleName());
            throw new InvalidCredentialsException();
        }

        UserDTO user = userService.findByEmail(request.getEmail());

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(SecurityUser.fromUser(user)))
                .build();
    }

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        UserDTO user = userService.createUser(request);

        return AuthenticationResponse.builder()
                .token(jwtService.generateToken(SecurityUser.fromUser(user)))
                .build();
    }
}
