package com.okhanzhin.task.tracker.scheduler.service;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.scheduler.client.TaskTrackerApiClient;
import com.okhanzhin.task.tracker.scheduler.config.properties.TaskTrackerApiClientProperties;
import com.okhanzhin.task.tracker.scheduler.exception.TaskTrackerClientException;
import com.okhanzhin.task.tracker.scheduler.mapper.SummarizationRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
@Service
@RequiredArgsConstructor
public class DailyReportRequestSource {

    private final TaskTrackerApiClient taskTrackerClient;
    private final TaskTrackerApiClientProperties taskTrackerClientProperties;
    private final SummarizationRequestMapper summarizationRequestMapper;

    public void forEach(Instant from, Instant to, Consumer<SummarizationRequest> requestConsumer) {

        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        Objects.requireNonNull(requestConsumer, "requestConsumer must not be null");

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }

        int pageSize = taskTrackerClientProperties.getDailyReportDataPageSize();

        Long afterUserId = null;

        do {
            DailyReportDataPage page =
                    taskTrackerClient.getDailyReportData(
                            from,
                            to,
                            pageSize,
                            afterUserId
                    );

            validatePage(page, from, to);
            Long nextAfterUserId = page.getNextAfterUserId();
            validateCursor(afterUserId, nextAfterUserId);

            page.getItems().stream()
                    .map(item ->
                            summarizationRequestMapper.toRequest(
                                    page.getPeriod(),
                                    item
                            )
                    )
                    .forEach(requestConsumer);

            afterUserId = nextAfterUserId;
        } while (afterUserId != null);
    }

    private void validatePage(DailyReportDataPage page, Instant from, Instant to) {
        if (page == null || page.getPeriod() == null || page.getItems() == null) {
            throw new TaskTrackerClientException(
                    "Task-tracker-backend returned an invalid report page"
            );
        }

        if (!from.equals(page.getPeriod().getFrom()) || !to.equals(page.getPeriod().getTo())) {
            throw new TaskTrackerClientException(
                    "Task-tracker-backend returned a report page "
                            + "for an unexpected period"
            );
        }
    }

    private void validateCursor(Long currentCursor, Long nextCursor) {
        if (currentCursor != null && nextCursor != null && nextCursor <= currentCursor) {
            throw new TaskTrackerClientException(
                    "Task-tracker-backend returned "
                            + "a non-advancing report cursor"
            );
        }
    }
}
