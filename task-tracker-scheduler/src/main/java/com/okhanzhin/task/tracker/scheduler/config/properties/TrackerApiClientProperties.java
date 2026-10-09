package com.okhanzhin.task.tracker.scheduler.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

/**
 *
 * @author okhanzhin on 05.10.2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "clients.task-tracker-backend")
public class TrackerApiClientProperties {

    @NotNull
    private URI baseUrl;

    @NotBlank
    private String dailyReportDataPath;

    @Min(1)
    private int dailyReportDataPageSize;

    @NotBlank
    @ToString.Exclude
    private String serviceToken;

    private Duration connectTimeout = Duration.ofSeconds(2);

    private Duration readTimeout = Duration.ofSeconds(2);

}
