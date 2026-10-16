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
public class UserReportData {

    private Long userId;
    private String userEmail;
    private List<TaskData> completedTasks;
    private List<TaskData> pendingTasks;
}
