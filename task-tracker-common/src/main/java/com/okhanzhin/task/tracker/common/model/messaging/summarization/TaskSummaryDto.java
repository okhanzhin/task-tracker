package com.okhanzhin.task.tracker.common.model.messaging.summarization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 *
 * @author okhanzhin on 23.09.2026
 */
@Value
@Builder
@Jacksonized
public class TaskSummaryDto {

    @NotNull
    Long id;

    @NotBlank
    String title;

    boolean completed;
}
