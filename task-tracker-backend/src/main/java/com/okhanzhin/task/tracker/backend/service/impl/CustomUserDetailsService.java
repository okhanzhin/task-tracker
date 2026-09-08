package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.dto.SecurityUser;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDAO user = userRepository.findByEmail(username).orElseThrow(() ->
                new UsernameNotFoundException(
                        String.format("User '%s' not found", username)
                ));

        return SecurityUser.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .isActive(user.getStatus().equals(Status.ACTIVE))
                .authorities(Collections.emptyList())
                .build();
    }
}
