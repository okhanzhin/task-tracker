package com.okhanzhin.email.sender.service.contract;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
public class EmailSendingTaskKafkaContractTest {

    @Test
    void shouldRoundTripWithoutTypeHeaders() {
        EmailSendingTask expected = EmailSendingTask.builder()
                .messageId(UUID.randomUUID())
                .recipientEmail("user@example.com")
                .subject("Daily report")
                .body("Completed: 2, pending: 1")
                .build();

        JsonSerializer<EmailSendingTask> serializer =
                new JsonSerializer<>();
        serializer.setAddTypeInfo(false);

        JsonDeserializer<EmailSendingTask> deserializer =
                new JsonDeserializer<>(EmailSendingTask.class, false);

        RecordHeaders headers = new RecordHeaders();

        byte[] payload = serializer.serialize(
                "EMAIL_SENDING_TASKS",
                headers,
                expected
        );

        EmailSendingTask actual = deserializer.deserialize(
                "EMAIL_SENDING_TASKS",
                headers,
                payload
        );

        assertThat(headers.lastHeader("__TypeId__")).isNull();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);

        serializer.close();
        deserializer.close();
    }
}
