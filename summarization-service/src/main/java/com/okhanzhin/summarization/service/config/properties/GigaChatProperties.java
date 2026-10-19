package com.okhanzhin.summarization.service.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.gigachat")
public class GigaChatProperties {

    @NotNull
    private URI authBaseUrl;

    @NotNull
    private URI apiBaseUrl;

    @NotBlank
    private String authorizationKey;

    @NotBlank
    private String scope;

    @NotBlank
    private String model;

    @NotNull
    private Duration connectTimeout;

    @NotNull
    private Duration readTimeout;

    @NotNull
    private Duration tokenRefreshMargin;

    @AssertTrue(message = "Connect timeout must be positive")
    public boolean isConnectTimeoutValid() {
        return connectTimeout == null || connectTimeout.compareTo(Duration.ZERO) > 0;
    }

    @AssertTrue(message = "Read timeout must be positive")
    public boolean isReadTimeoutValid() {
        return readTimeout == null || readTimeout.compareTo(Duration.ZERO) > 0;
    }

    @AssertTrue(message = "Token refresh margin must not be negative")
    public boolean isTokenRefreshMarginValid() {
        return tokenRefreshMargin == null
                || tokenRefreshMargin.compareTo(Duration.ZERO) >= 0;
    }
}
