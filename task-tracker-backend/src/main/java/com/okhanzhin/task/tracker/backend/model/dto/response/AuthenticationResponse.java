package com.okhanzhin.task.tracker.backend.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Ответ аутентификации, содержащий access token
 *
 * @author okhanzhin on 30.08.2026
 */
@Data
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
    private String token;
}
