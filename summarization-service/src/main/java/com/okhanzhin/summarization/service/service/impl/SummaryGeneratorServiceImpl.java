package com.okhanzhin.summarization.service.service.impl;

import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.request.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.request.TaskSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Slf4j
@Service
public class SummaryGeneratorServiceImpl implements SummaryGeneratorService {

    @Override
    public String generate(SummarizationRequest request) {
        List<TaskSummaryDto> tasks = request.getTasks();

        long completedCount = tasks.stream()
                .filter(TaskSummaryDto::isCompleted)
                .count();
        long uncompletedCount = tasks.stream()
                .filter(taskSummaryDto -> !taskSummaryDto.isCompleted())
                .count();

        String summary = """
        Your task report for %s — %s.

        You completed %d task(s).
        You still have %d incomplete task(s).
        Total tasks included in this report: %d.

        Keep up the great work!
        """.formatted(
                request.getPeriodStart(),
                request.getPeriodEnd(),
                completedCount,
                uncompletedCount,
                tasks.size()
        );

        log.info(
                "Generated stub summary: requestId={}, userId={}, completed={}, uncompleted={}",
                request.getRequestId(),
                request.getUserId(),
                completedCount,
                uncompletedCount
        );

        return summary;
    }
}
