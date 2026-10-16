package com.okhanzhin.task.tracker.scheduler.integration;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.listener.SummarizationResponseListener;
import com.okhanzhin.task.tracker.scheduler.producer.SummarizationRequestProducer;
import com.okhanzhin.task.tracker.scheduler.rpc.PendingSummarizationRequestRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Testcontainers
public class SummarizationRpcKafkaTest {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    private final BlockingQueue<SummarizationRequest> receivedRequests =
            new LinkedBlockingQueue<>();

    private final List<KafkaMessageListenerContainer<?, ?>> containers =
            new ArrayList<>();

    private DefaultKafkaProducerFactory<String, Object> producerFactory;
    private KafkaTemplate<String, Object> kafkaTemplate;

    private AppKafkaProperties kafkaProperties;
    private PendingSummarizationRequestRegistry registry;
    private SummarizationRequestProducer producer;

    private String requestTopic;
    private String responseTopic;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString();

        requestTopic = "rpc-requests-" + suffix;
        responseTopic = "rpc-responses-" + suffix;

        createTopics();
        createKafkaTemplate();

        kafkaProperties = mock(
                AppKafkaProperties.class,
                RETURNS_DEEP_STUBS
        );

        when(kafkaProperties.getTopics().getSummarizationRequest())
                .thenReturn(requestTopic);

        when(kafkaProperties.getRpc().getResponseTimeout())
                .thenReturn(Duration.ofSeconds(30));

        registry = new PendingSummarizationRequestRegistry(
                kafkaProperties
        );

        producer = new SummarizationRequestProducer(
                kafkaTemplate,
                registry,
                kafkaProperties
        );

        SummarizationResponseListener responseListener =
                new SummarizationResponseListener(registry);

        startListener(
                requestTopic,
                SummarizationRequest.class,
                request -> receivedRequests.add(request)
        );

        startListener(
                responseTopic,
                SummarizationResponse.class,
                responseListener::handle
        );
    }

    @AfterEach
    void tearDown() {
        try {
            for (KafkaMessageListenerContainer<?, ?> container : containers) {
                container.stop();
            }
        } finally {
            if (producerFactory != null) {
                producerFactory.destroy();
            }
        }
    }

    @Test
    void shouldCorrelateResponsesReceivedInReverseOrder()
            throws Exception {
        SummarizationRequest firstRequest = createRequest(1L);
        SummarizationRequest secondRequest = createRequest(2L);

        CompletableFuture<SummarizationResponse> firstFuture =
                producer.send(firstRequest);

        CompletableFuture<SummarizationResponse> secondFuture =
                producer.send(secondRequest);

        SummarizationRequest firstReceived = takeRequest();
        SummarizationRequest secondReceived = takeRequest();

        assertEquals(
                List.of(
                        firstRequest.getRequestId(),
                        secondRequest.getRequestId()
                ),
                List.of(
                        firstReceived.getRequestId(),
                        secondReceived.getRequestId()
                )
        );

        assertEquals(responseTopic, firstReceived.getReplyTopic());
        assertEquals(responseTopic, secondReceived.getReplyTopic());
        assertEquals(2, registry.size());

        SummarizationResponse secondResponse = createResponse(
                secondReceived,
                "Second user report"
        );

        kafkaTemplate.send(
                secondReceived.getReplyTopic(),
                secondReceived.getUserId().toString(),
                secondResponse
        ).get(10, TimeUnit.SECONDS);

        assertEquals(
                secondResponse,
                secondFuture.get(10, TimeUnit.SECONDS)
        );

        assertFalse(firstFuture.isDone());

        SummarizationResponse firstResponse = createResponse(
                firstReceived,
                "First user report"
        );

        kafkaTemplate.send(
                firstReceived.getReplyTopic(),
                firstReceived.getUserId().toString(),
                firstResponse
        ).get(10, TimeUnit.SECONDS);

        assertEquals(
                firstResponse,
                firstFuture.get(10, TimeUnit.SECONDS)
        );

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertEquals(0, registry.size())
                );
    }

    @Test
    void shouldTimeoutAndRemoveRequestWithoutResponse()
            throws Exception {
        when(kafkaProperties.getRpc().getResponseTimeout())
                .thenReturn(Duration.ofSeconds(3));

        SummarizationRequest request = createRequest(1L);

        CompletableFuture<SummarizationResponse> responseFuture =
                producer.send(request);

        SummarizationRequest received = takeRequest();

        assertEquals(
                request.getRequestId(),
                received.getRequestId()
        );

        ExecutionException exception = assertThrows(
                ExecutionException.class,
                () -> responseFuture.get(10, TimeUnit.SECONDS)
        );

        assertInstanceOf(
                TimeoutException.class,
                exception.getCause()
        );

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() ->
                        assertEquals(0, registry.size())
                );
    }

    private void createTopics() throws Exception {
        Map<String, Object> properties = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers()
        );

        try (AdminClient admin = AdminClient.create(properties)) {
            admin.createTopics(
                    List.of(
                            new NewTopic(requestTopic, 1, (short) 1),
                            new NewTopic(responseTopic, 1, (short) 1)
                    )
            ).all().get(20, TimeUnit.SECONDS);
        }
    }

    private void createKafkaTemplate() {
        Map<String, Object> properties = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers(),

                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class,

                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class,

                ProducerConfig.ACKS_CONFIG,
                "all",

                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true,

                ProducerConfig.MAX_BLOCK_MS_CONFIG,
                10000,

                ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG,
                5000,

                ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG,
                15000,

                JsonSerializer.ADD_TYPE_INFO_HEADERS,
                false
        );

        producerFactory = new DefaultKafkaProducerFactory<>(
                properties
        );

        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    private <T> void startListener(
            String topic,
            Class<T> payloadType,
            Consumer<T> handler
    ) {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers(),

                ConsumerConfig.GROUP_ID_CONFIG,
                "rpc-test-" + UUID.randomUUID(),

                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false,

                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        JsonDeserializer<T> valueDeserializer =
                new JsonDeserializer<>(payloadType, false);

        DefaultKafkaConsumerFactory<String, T> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        properties,
                        new StringDeserializer(),
                        valueDeserializer
                );

        ContainerProperties containerProperties =
                new ContainerProperties(
                        new TopicPartitionOffset(topic, 0, 0L)
                );

        containerProperties.setAckMode(
                ContainerProperties.AckMode.RECORD
        );
        containerProperties.setPollTimeout(200);

        containerProperties.setMessageListener(
                (MessageListener<String, T>) record ->
                        handler.accept(record.value())
        );

        KafkaMessageListenerContainer<String, T> container =
                new KafkaMessageListenerContainer<>(
                        consumerFactory,
                        containerProperties
                );

        container.setBeanName("rpc-test-" + topic);

        containers.add(container);
        container.start();
    }

    private SummarizationRequest takeRequest()
            throws InterruptedException {
        SummarizationRequest request =
                receivedRequests.poll(10, TimeUnit.SECONDS);

        assertNotNull(
                request,
                "Expected a summarization request in Kafka"
        );

        return request;
    }

    private SummarizationRequest createRequest(Long userId) {
        return SummarizationRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .userId(userId)
                .userEmail("user" + userId + "@example.com")
                .periodStart(
                        LocalDateTime.parse("2026-10-15T00:00:00")
                )
                .periodEnd(
                        LocalDateTime.parse("2026-10-16T00:00:00")
                )
                .replyTopic(responseTopic)
                .build();
    }

    private SummarizationResponse createResponse(
            SummarizationRequest request,
            String summary
    ) {
        return SummarizationResponse.builder()
                .requestId(request.getRequestId())
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .summary(summary)
                .success(true)
                .build();
    }
}
