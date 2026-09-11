package com.okhanzhin.task.tracker.backend.utils;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author okhanzhin on 08.09.2026
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static UserDAO user(
            String username,
            String email,
            String rawPassword,
            String timezone,
            PasswordEncoder passwordEncoder
    ) {
        return UserDAO.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .status(Status.ACTIVE)
                .timezone(timezone)
                .build();
    }

    public static UserDAO savedUser(
            String username,
            String email,
            String rawPassword,
            String timezone,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        UserDAO user = user(
                username,
                email,
                rawPassword,
                timezone,
                passwordEncoder
        );

        return userRepository.saveAndFlush(user);
    }
}
