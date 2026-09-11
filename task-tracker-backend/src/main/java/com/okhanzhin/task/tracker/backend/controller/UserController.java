package com.okhanzhin.task.tracker.backend.controller;

import com.okhanzhin.task.tracker.backend.model.dto.SecurityUser;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author okhanzhin on 25.08.2026
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal SecurityUser principal) {
        UserResponse userResponse = userService.findByUserDetails(principal);

        return ResponseEntity.ok(userResponse);
    }
}
