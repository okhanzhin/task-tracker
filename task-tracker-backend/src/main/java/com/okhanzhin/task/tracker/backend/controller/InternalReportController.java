package com.okhanzhin.task.tracker.backend.controller;

import com.okhanzhin.task.tracker.backend.service.DailyReportDataService;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 05.10.2026
 */
@RestController
@RequestMapping("/internal/v1/reports")
@RequiredArgsConstructor
public class InternalReportController {

    private final DailyReportDataService dailyReportDataService;

    @GetMapping("/daily-data")
    public ResponseEntity<?> getAllUserTasksByUserId(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @RequestParam(value = "limit", defaultValue = "100")
            int limit,
            @RequestParam(value = "afterUserId", required = false)
            Long afterUserId
    ) {
        DailyReportDataPage page = dailyReportDataService.getDailyReportData(from, to, limit, afterUserId);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(page);
    }
}
