package com.okhanzhin.task.tracker.backend.utils;

import com.okhanzhin.task.tracker.backend.exception.report.InvalidReportCursorException;
import com.okhanzhin.task.tracker.backend.exception.report.InvalidReportLimitException;
import com.okhanzhin.task.tracker.backend.exception.report.InvalidReportPeriodException;
import lombok.experimental.UtilityClass;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
@UtilityClass
public class ReportRequestValidator {

    private static final int MAX_LIMIT = 500;

    public void validateRequest(
            Instant from,
            Instant to,
            int limit,
            Long afterUserId
    ) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new InvalidReportPeriodException();
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            throw new InvalidReportLimitException();
        }

        if (afterUserId != null && afterUserId <= 0) {
            throw new InvalidReportCursorException();
        }
    }
}
