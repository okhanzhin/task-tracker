package com.okhanzhin.task.tracker.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
@SpringBootTest(properties = {
        "clients.task-tracker-backend.base-url=http://localhost:8080",
        "clients.task-tracker-backend.daily-report-data-path=/internal/v1/reports/daily-data",
        "clients.task-tracker-backend.daily-report-data-page-size=100",
        "clients.task-tracker-backend.service-token=test-token",
        "spring.kafka.listener.auto-startup=false"
})
public class TaskTrackerSchedulerApplicationTest {

    @Test
    void contextLoads() {
    }
}
