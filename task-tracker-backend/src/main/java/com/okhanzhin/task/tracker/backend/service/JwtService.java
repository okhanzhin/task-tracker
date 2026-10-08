package com.okhanzhin.task.tracker.backend.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
public interface JwtService {

    String generateToken(UserDetails userDetails);

    String generateSchedulerToken();

    Claims getClaims(String token);

    String getUsername(String token);
}
