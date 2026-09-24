package com.okhanzhin.task.tracker.common.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SummarizationResponse {

    private String requestId;
    private Long userId;
    private String userEmail;
    private String summary;
    private boolean success;
    private String errorMessage;
}
