package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.model.entity.RevokedToken;
import com.okhanzhin.task.tracker.backend.repository.RevokedTokenRepository;
import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.service.TokenRevocationService;
import com.okhanzhin.task.tracker.backend.utils.JwtClaims;
import io.jsonwebtoken.Claims;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
@Service
@RequiredArgsConstructor
public class TokenRevocationServiceImpl implements TokenRevocationService {

    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    @Transactional
    public void revoke(String token) {
        Claims claims = jwtService.getClaims(token);

        if (!JwtClaims.TYPE_USER.equals(
                claims.get(JwtClaims.TYPE, String.class)
        )) {
            throw new BadCredentialsException(
                    "Only user tokens can be logged out"
            );
        }

        String jti = requireJti(claims);

        if (claims.getExpiration() == null) {
            throw new BadCredentialsException(
                    "JWT expiration is missing"
            );
        }

        revokedTokenRepository.save(
                new RevokedToken(
                        jti,
                        claims.getExpiration().toInstant()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void checkNotRevoked(Claims claims) {
        String jti = requireJti(claims);

        if (revokedTokenRepository.existsById(jti)) {
            throw new BadCredentialsException("Token revoked");
        }
    }

    private String requireJti(Claims claims) {
        String jti = claims.getId();

        if (jti == null || jti.isBlank()) {
            throw new BadCredentialsException(
                    "JWT ID is missing"
            );
        }

        return jti;
    }
}
