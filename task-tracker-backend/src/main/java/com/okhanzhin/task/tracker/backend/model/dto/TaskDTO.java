package com.okhanzhin.task.tracker.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 *
 * @author okhanzhin on 10.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long taskId;
    private String title;
    private String description;
    private boolean completed;
    private OffsetDateTime completedAt;

}
