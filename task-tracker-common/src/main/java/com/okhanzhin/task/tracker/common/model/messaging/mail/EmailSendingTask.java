package com.okhanzhin.task.tracker.common.model.messaging.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 *
 * @author okhanzhin on 20.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailSendingTask {

    @NotNull(message = "messageId must not be null")
    private UUID messageId;

    @NotBlank(message = "recipientEmail must not be blank")
    @Email(message = "recipientEmail has invalid format")
    private String recipientEmail;

    @NotBlank(message = "subject must not be blank")
    private String subject;

    @NotBlank(message = "body must not be blank")
    private String body;
}
