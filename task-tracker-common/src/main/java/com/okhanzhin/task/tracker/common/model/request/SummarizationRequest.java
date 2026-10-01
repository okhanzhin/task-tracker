package com.okhanzhin.task.tracker.common.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummarizationRequest {

    private String requestId;
    private String replyTopic;
    private Long userId;
    private String userEmail;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private List<TaskSummaryDto> tasks;
}
