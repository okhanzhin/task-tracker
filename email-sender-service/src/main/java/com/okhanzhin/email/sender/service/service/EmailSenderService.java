package com.okhanzhin.email.sender.service.service;

import com.okhanzhin.email.sender.service.model.EmailNotification;

/**
 *
 * @author okhanzhin on 18.09.2026
 */
public interface EmailSenderService {

    void send(EmailNotification notification);
}
