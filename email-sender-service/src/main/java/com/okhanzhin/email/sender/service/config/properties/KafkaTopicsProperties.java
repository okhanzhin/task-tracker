package com.okhanzhin.email.sender.service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@ConfigurationProperties(prefix = "app.kafka.topics")
@Data
public class KafkaTopicsProperties {

    private String emailSending;
}
