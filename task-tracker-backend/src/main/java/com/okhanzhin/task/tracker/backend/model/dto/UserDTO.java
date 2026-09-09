package com.okhanzhin.task.tracker.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.okhanzhin.task.tracker.backend.model.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author okhanzhin on 31.08.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Status status;

}
