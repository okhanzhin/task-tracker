package com.okhanzhin.task.tracker.backend.mapper;

import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.TaskResponse;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.model.entity.projection.ReportTaskView;
import com.okhanzhin.task.tracker.common.model.report.TaskData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 *
 * @author okhanzhin on 10.09.2026
 */
@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final Clock clock;

    public TaskDAO map(UserDAO owner, CreateTaskRequest request) {
        boolean isCompleted = Boolean.TRUE.equals(request.getCompleted());
        Instant completedAt = isCompleted ? Instant.now(clock) : null;

        return TaskDAO.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .owner(owner)
                .completed(isCompleted)
                .completedAt(completedAt)
                .build();
    }

    public TaskResponse toResponse(TaskDAO task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public TaskData toTaskData(ReportTaskView taskView) {
        return new TaskData(
                taskView.getId(),
                taskView.getTitle(),
                taskView.getDescription(),
                taskView.getCompletedAt()
        );
    }
}
