package com.okhanzhin.task.tracker.backend.security;

import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.service.impl.CustomUserDetailsService;
import com.okhanzhin.task.tracker.backend.utils.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.okhanzhin.task.tracker.backend.utils.Constants.BEARER_PREFIX;
import static com.okhanzhin.task.tracker.backend.utils.JwtClaims.TYPE_USER;

/**
 *
 * @author okhanzhin on 31.08.2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint entryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (jwtToken.isEmpty()) {
            sendUnauthorized(request, response, new BadCredentialsException("JWT token is empty"));
            return;
        }

        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtService.getClaims(jwtToken);
                Authentication authentication = createAuthentication(claims, request);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("Invalid JWT token - {}", exception.getMessage());
            SecurityContextHolder.clearContext();
            sendUnauthorized(request, response, new BadCredentialsException("Invalid JWT token"));
        }
    }

    private Authentication createAuthentication(Claims claims, HttpServletRequest request) {
        String subject = claims.getSubject();

        return switch (claims.get(JwtClaims.TYPE, String.class)) {
            case JwtClaims.TYPE_USER -> createUserAuthentication(subject, request);
            case JwtClaims.TYPE_SERVICE -> createServiceAuthentication(
                    claims,
                    subject,
                    request
            );
            case null, default -> throw new BadCredentialsException(
                    "Unsupported JWT token type"
            );
        };
    }

    private Authentication createUserAuthentication(String subject, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        return authentication;
    }

    private Authentication createServiceAuthentication(Claims claims, String subject, HttpServletRequest request) {
        String scope = claims.get(JwtClaims.SCOPE, String.class);

        if (scope == null || scope.isBlank()) {
            throw new BadCredentialsException("Service JWT does not contain a scope");
        }

        List<SimpleGrantedAuthority> authorities = Arrays.stream(scope.split("\\s+"))
                .filter(value -> !value.isBlank())
                .map(value -> new SimpleGrantedAuthority("SCOPE_" + value))
                .toList();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        return authentication;
    }

    private void sendUnauthorized(HttpServletRequest request,
                                  HttpServletResponse response,
                                  BadCredentialsException exception) throws IOException, ServletException {
        entryPoint.commence(request, response, exception);
    }
}
