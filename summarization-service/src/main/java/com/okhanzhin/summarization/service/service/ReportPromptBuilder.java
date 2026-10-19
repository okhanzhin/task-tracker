package com.okhanzhin.summarization.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.okhanzhin.summarization.service.utils.Constants.SYSTEM_PROMPT;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Component
@RequiredArgsConstructor
public class ReportPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(SummarizationRequest request) {
        validateRequest(request);

        List<TaskSummaryDto> tasks = request.getTasks();

        List<String> completedTasks = tasks.stream()
                .filter(TaskSummaryDto::isCompleted)
                .map(TaskSummaryDto::getTitle)
                .toList();

        List<String> incompleteTasks = tasks.stream()
                .filter(task -> !task.isCompleted())
                .map(TaskSummaryDto::getTitle)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("periodStartInclusive", request.getPeriodStart().toString());
        data.put("periodEndExclusive", request.getPeriodEnd().toString());
        data.put("completedCount", completedTasks.size());
        data.put("incompleteCount", incompleteTasks.size());
        data.put("totalCount", tasks.size());
        data.put("completedTasks", completedTasks);
        data.put("incompleteTasks", incompleteTasks);

        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize task report data",
                    exception
            );
        }
    }

    private void validateRequest(SummarizationRequest request) {
        Assert.notNull(request, "Summarization request must not be null");
        Assert.notNull(request.getPeriodStart(), "Period start must not be null");
        Assert.notNull(request.getPeriodEnd(), "Period end must not be null");
        Assert.isTrue(
                request.getPeriodStart().isBefore(request.getPeriodEnd()),
                "Period start must be before period end"
        );
        Assert.notNull(request.getTasks(), "Tasks must not be null");

        for (TaskSummaryDto task : request.getTasks()) {
            Assert.notNull(task, "Task must not be null");
            Assert.hasText(task.getTitle(), "Task title must not be blank");
        }
    }
}
