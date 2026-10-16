package com.okhanzhin.task.tracker.backend.service;

import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
public interface DailyReportDataService {

    DailyReportDataPage getDailyReportData(
            Instant from,
            Instant to,
            int limit,
            Long afterUserId
    );
}
