package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final Duration jwtLifetime;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.lifetime}") Duration jwtLifetime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.jwtLifetime = jwtLifetime;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        var issuedDate = new Date();
        var expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String getUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
