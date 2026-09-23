package com.okhanzhin.email.sender.service.config.properties;

import jakarta.mail.internet.InternetAddress;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author okhanzhin on 22.09.2026
 */
@Data
@Validated
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    @NotBlank(message = "app.mail.from must be configured")
    @Email(message = "app.mail.from must be a valid email address")
    private String from;

    @NotBlank(message = "app.mail.from-name must be configured")
    private String fromName;

    public InternetAddress fromAddress() {
        try {
            return new InternetAddress(from, fromName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 must be supported", exception);
        }
    }
}
