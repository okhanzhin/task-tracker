package com.okhanzhin.task.tracker.scheduler.service;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author okhanzhin on 11.11.2026
 */
 @Component
public class FallbackReportBuilder {

    private static final int MAX_TITLES_PER_GROUP = 5;

    public String build(SummarizationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        List<TaskSummaryDto> tasks = Objects.requireNonNull(request.getTasks(), "tasks must not be null");

        List<TaskSummaryDto> completed = tasks.stream()
                .filter(TaskSummaryDto::isCompleted)
                .toList();

        List<TaskSummaryDto> pending = tasks.stream()
                .filter(task -> !task.isCompleted())
                .toList();

        StringBuilder body = new StringBuilder();

        body.append("Daily task report\n\n");

        appendGroup(body, "Completed tasks", completed);

        body.append("\n");

        appendGroup(body, "Pending tasks", pending);

        body.append(
                "\nAI-generated analysis is temporarily unavailable. "
                        + "This report was generated directly from your task data.\n"
        );

        return body.toString();
    }

    private void appendGroup(
            StringBuilder body,
            String heading,
            List<TaskSummaryDto> tasks
    ) {
        body.append(heading)
                .append(": ")
                .append(tasks.size())
                .append("\n");

        if (tasks.isEmpty()) {
            body.append("No tasks.\n");
            return;
        }

        tasks.stream()
                .limit(MAX_TITLES_PER_GROUP)
                .forEach(task -> body
                        .append("- ")
                        .append(toSingleLine(task.getTitle()))
                        .append("\n")
                );

        int remaining = tasks.size() - MAX_TITLES_PER_GROUP;

        if (remaining > 0) {
            body.append("And ")
                    .append(remaining)
                    .append(" more.\n");
        }
    }

    private String toSingleLine(String title) {
        return Objects.requireNonNull(title, "title must not be null")
                .replace('\r', ' ')
                .replace('\n', ' ');
    }
}
