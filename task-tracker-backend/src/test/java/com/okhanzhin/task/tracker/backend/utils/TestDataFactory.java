package com.okhanzhin.task.tracker.backend.utils;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.TaskRepository;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author okhanzhin on 08.09.2026
 */
@UtilityClass
public class TestDataFactory {

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

    public static TaskDAO savedTask(
            String title,
            String description,
            boolean completed,
            UserDAO owner,
            TaskRepository taskRepository
    ) {
        TaskDAO task = new TaskDAO();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);
        task.setOwner(owner);

        return taskRepository.save(task);
    }
}
