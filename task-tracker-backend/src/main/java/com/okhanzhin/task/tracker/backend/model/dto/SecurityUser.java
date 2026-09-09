package com.okhanzhin.task.tracker.backend.model.dto;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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

    private final String username;
    private final String password;
    private final List<SimpleGrantedAuthority> authorities;
    private final boolean isActive;

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public static UserDetails fromUser(UserDTO userDTO) {
        return new User(
                userDTO.getEmail(), userDTO.getPassword(),
                userDTO.getStatus().equals(Status.ACTIVE),
                userDTO.getStatus().equals(Status.ACTIVE),
                userDTO.getStatus().equals(Status.ACTIVE),
                userDTO.getStatus().equals(Status.ACTIVE),
                List.of()
        );
    }
}
