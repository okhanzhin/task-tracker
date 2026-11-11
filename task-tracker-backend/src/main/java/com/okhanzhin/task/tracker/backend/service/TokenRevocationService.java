package com.okhanzhin.task.tracker.backend.service;

import io.jsonwebtoken.Claims;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
public interface TokenRevocationService {

    void revoke(String token);

    void checkNotRevoked(Claims claims);
}
