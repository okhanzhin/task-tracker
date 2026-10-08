package com.okhanzhin.task.tracker.backend.model.entity.projection;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
public interface ReportTaskView {

    Long getOwnerId();

    Long getId();

    String getTitle();

    String getDescription();

    Boolean getCompleted();

    Instant getCompletedAt();
}
