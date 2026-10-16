package com.okhanzhin.task.tracker.common.model.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportPeriod {

    private Instant from;
    private Instant to;
}
