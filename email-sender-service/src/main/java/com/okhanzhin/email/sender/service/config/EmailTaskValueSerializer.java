package com.okhanzhin.email.sender.service.config;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 *
 * @author okhanzhin on 11.11.2026
 */
public class EmailTaskValueSerializer extends DelegatingByTypeSerializer {

    public EmailTaskValueSerializer() {
        super(Map.of(
                byte[].class, new ByteArraySerializer(),
                EmailSendingTask.class,
                new JsonSerializer<EmailSendingTask>()
        ));
    }
}
