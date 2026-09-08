package com.okhanzhin.task.tracker.backend.controller;

import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author okhanzhin on 25.08.2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO userResponse = userService.getUser(userDetails);

        return ResponseEntity.ok(userResponse);
    }
}
