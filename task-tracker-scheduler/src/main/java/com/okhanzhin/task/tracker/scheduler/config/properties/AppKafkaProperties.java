package com.okhanzhin.task.tracker.scheduler.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author okhanzhin on 01.10.2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    @Valid
    private Topics topics = new Topics();

    @Valid
    private Retry retry = new Retry();

    @Data
    public static class Topics {

        @NotBlank
        private String emailSending;

        @NotBlank
        private String summarizationRequest;

        @NotBlank
        private String summarizationResponse;

        @Min(1)
        private short partitions = 3;

        @Min(1)
        private short replicas = 1;
    }

    @Data
    public static class Retry {

        @Min(0)
        private int maxRetries = 3;

        @Min(0)
        private long initialIntervalMs = 1_000L;

        @DecimalMin(value = "1.0")
        private double multiplier = 2.0;

        @Min(0)
        private long maxIntervalMs = 4_000L;
    }
}
