package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.scheduler.service.DailyReportJob;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@ExtendWith(MockitoExtension.class)
class DailyReportSchedulerTest {

    @Mock
    private DailyReportJob dailyReportJob;

    @Test
    void shouldUsePreviousCalendarDayInUtc() {
        DailyReportScheduler scheduler = createScheduler(
                "2026-10-16T00:00:00Z",
                "UTC"
        );

        scheduler.generatePreviousDayReports();

        verify(dailyReportJob).run(
                Instant.parse("2026-10-15T00:00:00Z"),
                Instant.parse("2026-10-16T00:00:00Z")
        );
    }

    @Test
    void shouldUseReportTimeZoneInsteadOfUtcDate() {
        DailyReportScheduler scheduler = createScheduler(
                "2026-10-15T21:00:00Z",
                "Europe/Moscow"
        );

        scheduler.generatePreviousDayReports();

        verify(dailyReportJob).run(
                Instant.parse("2026-10-14T21:00:00Z"),
                Instant.parse("2026-10-15T21:00:00Z")
        );
    }

    @Test
    void shouldUseCalendarBoundariesWhenTriggeredAfterMidnight() {
        DailyReportScheduler scheduler = createScheduler(
                "2026-10-16T08:30:00Z",
                "UTC"
        );

        scheduler.generatePreviousDayReports();

        verify(dailyReportJob).run(
                Instant.parse("2026-10-15T00:00:00Z"),
                Instant.parse("2026-10-16T00:00:00Z")
        );
    }

    @Test
    void shouldHandleShortDayDuringDaylightSavingTransition() {
        DailyReportScheduler scheduler = createScheduler(
                "2026-03-30T00:00:00Z",
                "Europe/Dublin"
        );

        scheduler.generatePreviousDayReports();

        verify(dailyReportJob).run(
                Instant.parse("2026-03-29T00:00:00Z"),
                Instant.parse("2026-03-29T23:00:00Z")
        );
    }

    @Test
    void shouldHandleLongDayDuringDaylightSavingTransition() {
        DailyReportScheduler scheduler = createScheduler(
                "2026-10-26T00:00:00Z",
                "Europe/Dublin"
        );

        scheduler.generatePreviousDayReports();

        verify(dailyReportJob).run(
                Instant.parse("2026-10-24T23:00:00Z"),
                Instant.parse("2026-10-26T00:00:00Z")
        );
    }

    @Test
    void shouldRejectInvalidTimeZone() {
        assertThrows(
                DateTimeException.class,
                () -> createScheduler(
                        "2026-10-16T00:00:00Z",
                        "Invalid/Zone"
                )
        );

        verifyNoInteractions(dailyReportJob);
    }

    @Test
    void shouldPropagateJobFailure() {
        Instant from = Instant.parse("2026-10-15T00:00:00Z");
        Instant to = Instant.parse("2026-10-16T00:00:00Z");

        RuntimeException failure =
                new RuntimeException("Backend request failed");

        doThrow(failure)
                .when(dailyReportJob)
                .run(from, to);

        DailyReportScheduler scheduler = createScheduler(
                "2026-10-16T00:00:00Z",
                "UTC"
        );

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                scheduler::generatePreviousDayReports
        );

        assertSame(failure, exception);
    }

    private DailyReportScheduler createScheduler(
            String currentInstant,
            String reportZone
    ) {
        Clock clock = Clock.fixed(
                Instant.parse(currentInstant),
                ZoneOffset.UTC
        );

        return new DailyReportScheduler(
                dailyReportJob,
                clock,
                reportZone
        );
    }
}