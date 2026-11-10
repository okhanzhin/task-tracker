package com.okhanzhin.task.tracker.backend.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Data
@Builder
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;
    private final boolean isActive;
}
