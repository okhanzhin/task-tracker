package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.utils.JwtClaims;
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

import static com.okhanzhin.task.tracker.backend.utils.Constants.TASK_TRACKER_SCHEDULER_TOKEN_SUBJECT;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final Duration jwtLifetime;
    private final Duration serviceJwtLifetime;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.lifetime}") Duration jwtLifetime,
            @Value("${jwt.service-lifetime}") Duration serviceJwtLifetime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.jwtLifetime = jwtLifetime;
        this.serviceJwtLifetime = serviceJwtLifetime;
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        var issuedDate = new Date();
        var expiredDate = new Date(issuedDate.getTime() + jwtLifetime.toMillis());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(JwtClaims.TYPE, JwtClaims.TYPE_USER)
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateSchedulerToken() {
        var issuedDate = new Date();
        var expiredDate = new Date(issuedDate.getTime() + serviceJwtLifetime.toMillis());

        return Jwts.builder()
                .subject(TASK_TRACKER_SCHEDULER_TOKEN_SUBJECT)
                .claim(JwtClaims.TYPE, JwtClaims.TYPE_SERVICE)
                .claim(JwtClaims.SCOPE, JwtClaims.SCOPE_REPORTS_READ)
                .issuedAt(issuedDate)
                .expiration(expiredDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Claims getClaims(String token) {
        return getAllClaimsFromToken(token);
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
