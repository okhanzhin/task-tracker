package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.producer.SummarizationRequestProducer;
import com.okhanzhin.task.tracker.scheduler.rpc.PendingSummarizationRequestRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@ExtendWith(MockitoExtension.class)
class SummarizationRequestProducerTest {

    private static final String TOPIC = "SUMMARIZATION_REQUESTS";
    private static final String REQUEST_ID = "report-request-1";
    private static final String USER_KEY = "1";

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private AppKafkaProperties kafkaProperties;

    private PendingSummarizationRequestRegistry registry;
    private SummarizationRequestProducer producer;

    @BeforeEach
    void setUp() {
        registry = new PendingSummarizationRequestRegistry(
                kafkaProperties
        );

        producer = new SummarizationRequestProducer(
                kafkaTemplate,
                registry,
                kafkaProperties
        );
    }

    @Test
    void shouldWaitForResponseAfterSuccessfulPublication() {
        configureProperties();

        SummarizationRequest request = createRequest();

        CompletableFuture<SendResult<String, Object>> publicationFuture =
                new CompletableFuture<>();

        when(kafkaTemplate.send(TOPIC, USER_KEY, request))
                .thenReturn(publicationFuture);

        CompletableFuture<SummarizationResponse> responseFuture =
                producer.send(request);

        assertEquals(1, registry.size());
        assertFalse(responseFuture.isDone());

        publicationFuture.complete(null);

        assertFalse(responseFuture.isDone());
        assertEquals(1, registry.size());

        SummarizationResponse response =
                mock(SummarizationResponse.class);

        when(response.getRequestId()).thenReturn(REQUEST_ID);

        assertTrue(registry.complete(response));

        assertSame(response, responseFuture.join());
        assertEquals(0, registry.size());
    }

    @Test
    void shouldCleanRegistryWhenPublicationFailsAsynchronously() {
        configureProperties();

        SummarizationRequest request = createRequest();

        CompletableFuture<SendResult<String, Object>> publicationFuture =
                new CompletableFuture<>();

        when(kafkaTemplate.send(TOPIC, USER_KEY, request))
                .thenReturn(publicationFuture);

        CompletableFuture<SummarizationResponse> responseFuture =
                producer.send(request);

        assertEquals(1, registry.size());

        RuntimeException failure =
                new RuntimeException("Kafka publication failed");

        publicationFuture.completeExceptionally(failure);

        assertTrue(responseFuture.isCompletedExceptionally());
        assertEquals(0, registry.size());

        CompletionException exception = assertThrows(
                CompletionException.class,
                responseFuture::join
        );

        assertSame(failure, exception.getCause());
    }

    @Test
    void shouldCleanRegistryWhenPublicationThrowsSynchronously() {
        configureProperties();

        SummarizationRequest request = createRequest();

        RuntimeException failure =
                new IllegalStateException(
                        "Kafka producer could not publish the request"
                );

        when(kafkaTemplate.send(TOPIC, USER_KEY, request))
                .thenThrow(failure);

        CompletableFuture<SummarizationResponse> responseFuture =
                producer.send(request);

        assertTrue(responseFuture.isCompletedExceptionally());
        assertEquals(0, registry.size());

        CompletionException exception = assertThrows(
                CompletionException.class,
                responseFuture::join
        );

        assertSame(failure, exception.getCause());
    }

    @Test
    void shouldRejectNullRequestBeforeRegistration() {
        assertThrows(
                NullPointerException.class,
                () -> producer.send(null)
        );

        assertEquals(0, registry.size());
        verifyNoInteractions(kafkaTemplate, kafkaProperties);
    }

    @Test
    void shouldRejectMissingUserIdBeforeRegistration() {
        SummarizationRequest request = SummarizationRequest.builder()
                .requestId(REQUEST_ID)
                .build();

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> producer.send(request)
        );

        assertEquals(
                "userId must not be null",
                exception.getMessage()
        );
        assertEquals(0, registry.size());

        verifyNoInteractions(kafkaTemplate, kafkaProperties);
    }

    private void configureProperties() {
        when(kafkaProperties.getTopics().getSummarizationRequest())
                .thenReturn(TOPIC);

        when(kafkaProperties.getRpc().getResponseTimeout())
                .thenReturn(Duration.ofSeconds(30));
    }

    private SummarizationRequest createRequest() {
        return SummarizationRequest.builder()
                .requestId(REQUEST_ID)
                .userId(1L)
                .userEmail("user@example.com")
                .build();
    }
}
