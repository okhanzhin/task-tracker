package com.okhanzhin.task.tracker.scheduler.contract;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static org.assertj.core.api.Assertions.assertThat;
/**
 *
 * @author okhanzhin on 15.10.2026
 */
public class SummarizationResponseKafkaContractTest {

    @Test
    void shouldRoundTripWithoutTypeHeaders() {
        SummarizationResponse expected =
                SummarizationResponse.builder()
                        .requestId("request-1")
                        .userId(42L)
                        .userEmail("user@example.com")
                        .summary(
                                "Two tasks completed; one remains."
                        )
                        .success(true)
                        .build();

        JsonSerializer<SummarizationResponse> serializer =
                new JsonSerializer<>();
        serializer.setAddTypeInfo(false);

        JsonDeserializer<SummarizationResponse> deserializer =
                new JsonDeserializer<>(
                        SummarizationResponse.class,
                        false
                );

        RecordHeaders headers = new RecordHeaders();

        byte[] payload = serializer.serialize(
                "SUMMARIZATION_RESPONSES",
                headers,
                expected
        );

        SummarizationResponse actual = deserializer.deserialize(
                "SUMMARIZATION_RESPONSES",
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
