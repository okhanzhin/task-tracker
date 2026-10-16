package com.okhanzhin.task.tracker.backend.utils;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.TaskRepository;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.common.model.report.TaskData;
import com.okhanzhin.task.tracker.common.model.report.UserReportData;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;

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
        return savedTask(
                title,
                description,
                completed ? Instant.now() : null,
                owner,
                taskRepository
        );
    }

    public static TaskDAO savedTask(
            String title,
            String description,
            Instant completedAt,
            UserDAO owner,
            TaskRepository taskRepository
    ) {
        TaskDAO task = new TaskDAO();
        task.setTitle(title);
        task.setDescription(description);
        task.setOwner(owner);
        task.setCompleted(completedAt != null);
        task.setCompletedAt(completedAt);

        return taskRepository.saveAndFlush(task);
    }

    public static List<Long> userIds(DailyReportDataPage page) {
        return page.getItems().stream()
                .map(UserReportData::getUserId)
                .toList();
    }

    public static List<Long> taskIds(List<TaskData> tasks) {
        return tasks.stream()
                .map(TaskData::getTaskId)
                .toList();
    }
}
