package com.okhanzhin.task.tracker.backend.model.dto.request;

import lombok.Data;

/**
 *
 * @author okhanzhin on 25.08.2026
 */
@Data
public class AuthenticationRequest {

    private String email;
    private String password;
}
