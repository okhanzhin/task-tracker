package com.okhanzhin.summarization.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GigaChatTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_at")
    private Long expiresAt;
}
