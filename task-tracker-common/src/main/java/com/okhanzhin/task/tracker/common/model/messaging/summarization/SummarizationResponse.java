package com.okhanzhin.task.tracker.common.model.messaging.summarization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Value
@Builder
@Jacksonized
public class SummarizationResponse {

    @NotBlank
    String requestId;

    @NotNull
    Long userId;

    String userEmail;
    String summary;
    boolean success;
    String errorMessage;
}
