package com.okhanzhin.task.tracker.backend.config.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret must not be blank")
    private String secret;

    @NotNull(message = "JWT lifetime must be configured")
    private Duration lifetime;

    @NotNull(message = "Service JWT lifetime must be configured")
    private Duration serviceLifetime;

    @AssertTrue(message = "JWT lifetime must be positive")
    public boolean isLifetimePositive() {
        return lifetime == null
                || (!lifetime.isNegative() && !lifetime.isZero());
    }

    @AssertTrue(message = "Service JWT lifetime must be positive")
    public boolean isServiceLifetimePositive() {
        return serviceLifetime == null
                || (!serviceLifetime.isNegative()
                && !serviceLifetime.isZero());
    }
}
