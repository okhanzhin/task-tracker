package com.okhanzhin.task.tracker.common.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportDataPage {

    private ReportPeriod period;
    private List<UserReportData> items;
    private Long nextAfterUserId;
}
