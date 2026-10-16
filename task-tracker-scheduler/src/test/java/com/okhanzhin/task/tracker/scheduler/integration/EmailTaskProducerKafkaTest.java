package com.okhanzhin.task.tracker.scheduler.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.scheduler.config.properties.AppKafkaProperties;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Testcontainers
class EmailTaskProducerKafkaTest {

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    private DefaultKafkaProducerFactory<String, Object> producerFactory;

    @AfterEach
    void tearDown() {
        if (producerFactory != null) {
            producerFactory.destroy();
        }
    }

    @Test
    void shouldPublishEmailTaskAsJson() throws Exception {
        String topic = "email-tasks-test-" + UUID.randomUUID();

        createTopic(topic);

        EmailTaskProducer producer = createProducer(topic);

        UUID messageId = UUID.randomUUID();

        EmailSendingTask task = EmailSendingTask.builder()
                .messageId(messageId)
                .recipientEmail("user@example.com")
                .subject("Daily task report")
                .body("Completed 3 tasks; 2 tasks remain incomplete.")
                .build();

        try (KafkaConsumer<String, String> consumer = createConsumer()) {
            TopicPartition partition = new TopicPartition(topic, 0);

            consumer.assign(List.of(partition));
            consumer.seekToBeginning(List.of(partition));

            var sendResult = producer.send(task)
                    .get(20, TimeUnit.SECONDS);

            assertNotNull(sendResult);
            assertEquals(
                    topic,
                    sendResult.getRecordMetadata().topic()
            );

            ConsumerRecord<String, String> record =
                    readSingleRecord(consumer);

            assertEquals(topic, record.topic());
            assertEquals(
                    task.getRecipientEmail(),
                    record.key()
            );

            assertNull(
                    record.headers().lastHeader("__TypeId__")
            );

            JsonNode json = objectMapper.readTree(record.value());

            assertEquals(
                    messageId.toString(),
                    json.path("messageId").asText()
            );
            assertEquals(
                    task.getRecipientEmail(),
                    json.path("recipientEmail").asText()
            );
            assertEquals(
                    task.getSubject(),
                    json.path("subject").asText()
            );
            assertEquals(
                    task.getBody(),
                    json.path("body").asText()
            );

            EmailSendingTask restored = objectMapper.readValue(
                    record.value(),
                    EmailSendingTask.class
            );

            assertEquals(task, restored);
        }
    }

    private void createTopic(String topic) throws Exception {
        Map<String, Object> properties = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers()
        );

        try (AdminClient adminClient = AdminClient.create(properties)) {
            adminClient.createTopics(
                    List.of(new NewTopic(topic, 1, (short) 1))
            ).all().get(20, TimeUnit.SECONDS);
        }
    }

    private EmailTaskProducer createProducer(String topic) {
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

        KafkaTemplate<String, Object> kafkaTemplate =
                new KafkaTemplate<>(producerFactory);

        AppKafkaProperties kafkaProperties = mock(
                AppKafkaProperties.class,
                RETURNS_DEEP_STUBS
        );

        when(kafkaProperties.getTopics().getEmailSending())
                .thenReturn(topic);

        return new EmailTaskProducer(
                kafkaTemplate,
                kafkaProperties
        );
    }

    private KafkaConsumer<String, String> createConsumer() {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                KAFKA.getBootstrapServers(),

                ConsumerConfig.GROUP_ID_CONFIG,
                "email-test-consumer-" + UUID.randomUUID(),

                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,

                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class,

                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false,

                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new KafkaConsumer<>(properties);
    }

    private ConsumerRecord<String, String> readSingleRecord(
            KafkaConsumer<String, String> consumer
    ) {
        List<ConsumerRecord<String, String>> received =
                new ArrayList<>();

        long deadline = System.nanoTime()
                + Duration.ofSeconds(15).toNanos();

        while (received.isEmpty() && System.nanoTime() < deadline) {
            consumer.poll(Duration.ofMillis(500))
                    .forEach(received::add);
        }

        assertEquals(
                1,
                received.size(),
                "Expected one email task record"
        );

        return received.get(0);
    }
}
