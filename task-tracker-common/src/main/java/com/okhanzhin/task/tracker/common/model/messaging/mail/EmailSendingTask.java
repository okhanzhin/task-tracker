package com.okhanzhin.task.tracker.common.model.messaging.mail;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    private UUID messageId;
    private String recipientEmail;
    private String subject;
    private String body;
}
