package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.scheduler.client.TaskTrackerApiClient;
import com.okhanzhin.task.tracker.scheduler.config.properties.TaskTrackerApiClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
public class TrackerApiClientTest {

    @Test
    void shouldRequestDailyReportPageWithCursor() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://backend:8080")
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer service-token"
                );

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        TaskTrackerApiClientProperties properties =
                new TaskTrackerApiClientProperties();
        properties.setDailyReportDataPath(
                "/internal/v1/reports/daily-data"
        );

        TaskTrackerApiClient client =
                new TaskTrackerApiClient(builder.build(), properties);

        server.expect(requestTo(
                        "http://backend:8080/internal/v1/reports/daily-data"
                                + "?from=2026-10-14T21:00:00Z"
                                + "&to=2026-10-15T21:00:00Z"
                                + "&limit=100"
                                + "&afterUserId=42"
                ))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer service-token"
                ))
                .andRespond(withSuccess("""
            {
              "period": {
                "from": "2026-10-14T21:00:00Z",
                "to": "2026-10-15T21:00:00Z"
              },
              "items": [],
              "nextAfterUserId": null
            }
            """, MediaType.APPLICATION_JSON));

        DailyReportDataPage page = client.getDailyReportData(
                Instant.parse("2026-10-14T21:00:00Z"),
                Instant.parse("2026-10-15T21:00:00Z"),
                100,
                42L
        );

        assertThat(page.getItems()).isEmpty();
        assertThat(page.getPeriod().getFrom())
                .isEqualTo(Instant.parse(
                        "2026-10-14T21:00:00Z"
                ));

        server.verify();
    }
}
