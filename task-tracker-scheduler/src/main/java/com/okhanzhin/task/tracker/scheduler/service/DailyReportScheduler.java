package com.okhanzhin.task.tracker.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Slf4j
@Component
public class DailyReportScheduler {

    private final DailyReportJob dailyReportJob;
    private final Clock clock;
    private final ZoneId reportZone;

    public DailyReportScheduler(DailyReportJob dailyReportJob,
                                @Qualifier("reportClock") Clock cLock,
                                @Value("${app.reports.daily.zone:UTC}") String reportZone) {
        this.dailyReportJob = dailyReportJob;
        this.clock = cLock;
        this.reportZone = ZoneId.of(reportZone);
    }

    @Scheduled(
            cron = "${app.reports.daily.cron:0 0 0 * * *}",
            zone = "${app.reports.daily.zone:UTC}"
    )
    public void generatePreviousDayReports() {
        LocalDate today = LocalDate.ofInstant(clock.instant(), reportZone);

        LocalDate reportDate = today.minusDays(1);
        Instant from = reportDate.atStartOfDay(reportZone).toInstant();
        Instant to = today.atStartOfDay(reportZone).toInstant();

        log.info(
                "Triggering daily reports: reportDate={}, zone={}, from={}, to={}",
                reportDate,
                reportZone,
                from,
                to
        );

        dailyReportJob.run(from, to);
    }
}
