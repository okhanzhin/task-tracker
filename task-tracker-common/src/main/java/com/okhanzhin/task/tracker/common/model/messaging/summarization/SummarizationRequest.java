package com.okhanzhin.task.tracker.common.model.messaging.summarization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Value
@Builder
@Jacksonized
public class SummarizationRequest {

    @NotBlank
    String requestId;

    @NotNull
    Long userId;

    @NotBlank
    @Email
    String userEmail;

    @NotNull
    LocalDateTime periodStart;

    @NotNull
    LocalDateTime periodEnd;

    @Valid
    @NotEmpty
    List<TaskSummaryDto> tasks;

    @NotNull
    String replyTopic;
}
