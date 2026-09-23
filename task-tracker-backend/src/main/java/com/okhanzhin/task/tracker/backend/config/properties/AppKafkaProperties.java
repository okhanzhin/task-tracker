package com.okhanzhin.task.tracker.backend.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {

    @Valid
    private Topics topics = new Topics();

    @Data
    public static class Topics {

        @NotBlank
        private String emailSending;
    }
}
