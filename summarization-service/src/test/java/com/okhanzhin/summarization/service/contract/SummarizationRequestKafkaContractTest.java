package com.okhanzhin.summarization.service.contract;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author okhanzhin on 15.10.2026
 */
public class SummarizationRequestKafkaContractTest {

    @Test
    void shouldRoundTripWithoutTypeHeaders() {
        SummarizationRequest expected =
                SummarizationRequest.builder()
                        .requestId("request-1")
                        .userId(42L)
                        .userEmail("user@example.com")
                        .periodStart(LocalDateTime.of(
                                2026, 10, 14, 0, 0
                        ))
                        .periodEnd(LocalDateTime.of(
                                2026, 10, 15, 0, 0
                        ))
                        .tasks(List.of(
                                TaskSummaryDto.builder()
                                        .id(7L)
                                        .title("Ship release")
                                        .completed(true)
                                        .build()
                        ))
                        .replyTopic("SUMMARIZATION_RESPONSES")
                        .build();

        JsonSerializer<SummarizationRequest> serializer =
                new JsonSerializer<>();
        serializer.setAddTypeInfo(false);

        JsonDeserializer<SummarizationRequest> deserializer =
                new JsonDeserializer<>(
                        SummarizationRequest.class,
                        false
                );

        RecordHeaders headers = new RecordHeaders();

        byte[] payload = serializer.serialize(
                "SUMMARIZATION_REQUESTS",
                headers,
                expected
        );

        SummarizationRequest actual = deserializer.deserialize(
                "SUMMARIZATION_REQUESTS",
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
