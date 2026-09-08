package com.okhanzhin.task.tracker.backend.service;

import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
public interface JwtService {

    String generateToken(UserDetails userDetails);

    String getUsername(String token);
}
