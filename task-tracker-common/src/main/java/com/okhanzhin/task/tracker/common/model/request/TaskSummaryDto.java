package com.okhanzhin.task.tracker.common.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 *
 * @author okhanzhin on 23.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskSummaryDto {

    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private LocalDateTime completedAt;
}
