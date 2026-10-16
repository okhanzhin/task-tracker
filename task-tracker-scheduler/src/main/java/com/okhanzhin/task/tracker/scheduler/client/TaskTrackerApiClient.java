package com.okhanzhin.task.tracker.scheduler.client;

import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.scheduler.config.properties.TaskTrackerApiClientProperties;
import com.okhanzhin.task.tracker.scheduler.exception.TaskTrackerClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.okhanzhin.task.tracker.scheduler.utils.Constants.AFTER_USER_ID_QUERY_PARAM_NAME;
import static com.okhanzhin.task.tracker.scheduler.utils.Constants.FROM_QUERY_PARAM_NAME;
import static com.okhanzhin.task.tracker.scheduler.utils.Constants.LIMIT_QUERY_PARAM_NAME;
import static com.okhanzhin.task.tracker.scheduler.utils.Constants.TO_QUERY_PARAM_NAME;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
@Component
@RequiredArgsConstructor
public class TaskTrackerApiClient {

    private final RestClient restClient;
    private final TaskTrackerApiClientProperties properties;

    public DailyReportDataPage getDailyReportData(
            Instant from,
            Instant to,
            int limit,
            Long afterUserId
    ) {
        try {
            DailyReportDataPage page = restClient
                    .get()
                    .uri(
                            uriBuilder -> {
                                uriBuilder
                                        .path(properties.getDailyReportDataPath())
                                        .queryParam(FROM_QUERY_PARAM_NAME, from)
                                        .queryParam(TO_QUERY_PARAM_NAME, to)
                                        .queryParam(LIMIT_QUERY_PARAM_NAME, limit);

                                if (afterUserId != null) {
                                    uriBuilder.queryParam(AFTER_USER_ID_QUERY_PARAM_NAME, afterUserId);
                                }

                                return uriBuilder.build();
                            }
                    )
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                int status = response.getStatusCode().value();
                                String body = new String(
                                        response.getBody().readAllBytes(),
                                        StandardCharsets.UTF_8
                                );

                                throw new TaskTrackerClientException(status, body);
                            })
                    .body(DailyReportDataPage.class);

            if (page == null) {
                throw new TaskTrackerClientException("Task-tracker-backend returned an empty response body");
            }

            return page;
        } catch (RestClientException exception) {
            throw new TaskTrackerClientException(
                    "Failed to retrieve daily report data from task-tracker-backend",
                    exception
            );
        }
    }
}
