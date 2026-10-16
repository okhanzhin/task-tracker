package com.okhanzhin.task.tracker.scheduler.mapper;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import com.okhanzhin.task.tracker.common.model.report.ReportPeriod;
import com.okhanzhin.task.tracker.common.model.report.TaskData;
import com.okhanzhin.task.tracker.common.model.report.UserReportData;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.exception.TaskTrackerClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
@Component
@RequiredArgsConstructor
public class SummarizationRequestMapper {

    private final AppKafkaProperties kafkaProperties;

    public SummarizationRequest toRequest(ReportPeriod period, UserReportData report) {
        if (period == null || period.getFrom() == null || period.getTo() == null) {
            throw new TaskTrackerClientException(
                    "Task-tracker-backend returned an invalid report period"
            );
        }

        if (report == null || report.getUserId() == null || report.getUserEmail() == null) {
            throw new TaskTrackerClientException(
                    "Task-tracker-backend returned invalid user report data"
            );
        }

        return SummarizationRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(report.getUserId())
                .userEmail(report.getUserEmail())
                .periodStart(LocalDateTime.ofInstant(
                        period.getFrom(),
                        ZoneOffset.UTC
                ))
                .periodEnd(LocalDateTime.ofInstant(
                        period.getTo(),
                        ZoneOffset.UTC
                ))
                .tasks(mapTasks(report))
                .replyTopic(
                        kafkaProperties.getTopics()
                                .getSummarizationResponse()
                )
                .build();
    }

    private List<TaskSummaryDto> mapTasks(UserReportData report) {
        List<TaskSummaryDto> tasks = new ArrayList<>();

        addTasks(tasks, report.getCompletedTasks(), true);
        addTasks(tasks, report.getPendingTasks(), false);

        return tasks;
    }

    private void addTasks(
            List<TaskSummaryDto> target,
            List<TaskData> source,
            boolean completed
    ) {
        if (source == null) {
            return;
        }

        source.stream()
                .map(task -> TaskSummaryDto.builder()
                        .id(task.getTaskId())
                        .title(task.getTitle())
                        .completed(completed)
                        .build())
                .forEach(target::add);
    }
}
