package com.okhanzhin.task.tracker.backend.controller;

import com.okhanzhin.task.tracker.backend.model.dto.request.AuthenticationRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.AuthenticationResponse;
import com.okhanzhin.task.tracker.backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.okhanzhin.task.tracker.backend.utils.Constants.BEARER_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authResponse = authenticationService.authenticate(request);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + authResponse.getToken())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthenticationResponse registerResponse = authenticationService.register(registerRequest);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, "Bearer " + registerResponse.getToken())
                .build();
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(AUTHORIZATION, "");
        SecurityContextHolder.clearContext();
    }
}
