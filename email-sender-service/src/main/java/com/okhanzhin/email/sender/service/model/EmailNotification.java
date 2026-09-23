package com.okhanzhin.email.sender.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 *
 * @author okhanzhin on 18.09.2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {

    private UUID messageId;
    private String recipientEmail;
    private String subject;
    private String body;
}
