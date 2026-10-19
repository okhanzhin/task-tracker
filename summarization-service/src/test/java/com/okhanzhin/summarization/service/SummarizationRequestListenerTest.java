package com.okhanzhin.summarization.service;

import com.okhanzhin.summarization.service.config.properties.AppKafkaProperties;
import com.okhanzhin.summarization.service.exception.GigaChatClientException;
import com.okhanzhin.summarization.service.listener.SummarizationRequestListener;
import com.okhanzhin.summarization.service.producer.SummarizationResponseProducer;
import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
public class SummarizationRequestListenerTest {

    private static final String REPLY_TOPIC =
            "summarization-response";

    private SummarizationResponseProducer responseProducer;
    private SummaryGeneratorService summaryGeneratorService;
    private SummarizationRequestListener listener;

    @BeforeEach
    void setUp() {
        AppKafkaProperties kafkaProperties =
                mock(AppKafkaProperties.class);

        responseProducer = mock(SummarizationResponseProducer.class);
        summaryGeneratorService = mock(SummaryGeneratorService.class);

        listener = new SummarizationRequestListener(
                kafkaProperties,
                responseProducer,
                summaryGeneratorService
        );
    }

    @Test
    void shouldSendSuccessfulResponse() {
        SummarizationRequest request = validRequest();
        String report = "Completed: 1\nIncomplete: 0\nTotal: 1";

        when(summaryGeneratorService.generate(request))
                .thenReturn(report);

        stubSuccessfulSend();

        listener.handle(request);

        SummarizationResponse response = captureResponse();

        assertThat(response.getRequestId())
                .isEqualTo(request.getRequestId());

        assertThat(response.getUserId())
                .isEqualTo(request.getUserId());

        assertThat(response.getUserEmail())
                .isEqualTo(request.getUserEmail());

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getSummary()).isEqualTo(report);
        assertThat(response.getErrorMessage()).isNull();

        verify(summaryGeneratorService).generate(request);
    }

    @Test
    void shouldSendFailureResponseWhenGenerationFails() {
        SummarizationRequest request = validRequest();

        when(summaryGeneratorService.generate(request))
                .thenThrow(new GigaChatClientException(
                        "Internal provider error details"
                ));

        stubSuccessfulSend();

        listener.handle(request);

        SummarizationResponse response = captureResponse();

        assertThat(response.getRequestId())
                .isEqualTo(request.getRequestId());

        assertThat(response.getUserId())
                .isEqualTo(request.getUserId());

        assertThat(response.getUserEmail())
                .isEqualTo(request.getUserEmail());

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getSummary()).isNull();

        assertThat(response.getErrorMessage())
                .isEqualTo("Failed to generate daily summary")
                .doesNotContain("Internal provider error details");

        verify(summaryGeneratorService).generate(request);
    }

    @Test
    void shouldPropagateResponseDeliveryFailure() {
        SummarizationRequest request = validRequest();

        when(summaryGeneratorService.generate(request))
                .thenReturn("Generated report");

        IllegalStateException failure =
                new IllegalStateException("Kafka delivery failed");

        CompletableFuture<SendResult<String, SummarizationResponse>>
                failedSend = CompletableFuture.failedFuture(failure);

        when(responseProducer.send(
                eq(REPLY_TOPIC),
                any(SummarizationResponse.class)
        )).thenReturn(failedSend);

        assertThatThrownBy(() -> listener.handle(request))
                .isInstanceOf(CompletionException.class)
                .hasCause(failure);

        verify(summaryGeneratorService).generate(request);
        verify(responseProducer).send(
                eq(REPLY_TOPIC),
                any(SummarizationResponse.class)
        );
    }

    private void stubSuccessfulSend() {
        CompletableFuture<SendResult<String, SummarizationResponse>>
                completedSend = CompletableFuture.completedFuture(null);

        when(responseProducer.send(
                eq(REPLY_TOPIC),
                any(SummarizationResponse.class)
        )).thenReturn(completedSend);
    }

    private SummarizationResponse captureResponse() {
        ArgumentCaptor<SummarizationResponse> captor =
                ArgumentCaptor.forClass(SummarizationResponse.class);

        verify(responseProducer).send(
                eq(REPLY_TOPIC),
                captor.capture()
        );

        return captor.getValue();
    }

    private SummarizationRequest validRequest() {
        return SummarizationRequest.builder()
                .requestId("request-1")
                .userId(42L)
                .userEmail("user@example.com")
                .periodStart(LocalDateTime.of(2026, 10, 16, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 17, 0, 0))
                .replyTopic(REPLY_TOPIC)
                .tasks(List.of(
                        TaskSummaryDto.builder()
                                .id(1L)
                                .title("Implement registration")
                                .completed(true)
                                .build()
                ))
                .build();
    }
}
