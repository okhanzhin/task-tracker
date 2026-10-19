package com.okhanzhin.summarization.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.summarization.service.client.GigaChatAuthClient;
import com.okhanzhin.summarization.service.client.GigaChatClient;
import com.okhanzhin.summarization.service.client.GigaChatTokenProvider;
import com.okhanzhin.summarization.service.config.GigaChatConfig;
import com.okhanzhin.summarization.service.config.properties.GigaChatProperties;
import com.okhanzhin.summarization.service.service.ReportPromptBuilder;
import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.summarization.service.service.impl.SummaryGeneratorServiceImpl;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
@SpringBootTest(
        classes = GigaChatClientManualTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@EnabledIfEnvironmentVariable(
        named = "GIGACHAT_MANUAL_TEST_ENABLED",
        matches = "true"
)
public class GigaChatClientManualTest {

    @Autowired
    private SummaryGeneratorService summaryGeneratorService;

    @Test
    void shouldGenerateReportFromSummarizationRequest() {
        SummarizationRequest request = SummarizationRequest.builder()
                .requestId("manual-report-1")
                .userId(1L)
                .userEmail("test@example.com")
                .periodStart(LocalDateTime.of(2026, 10, 16, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 17, 0, 0))
                .replyTopic("summarization-response")
                .tasks(List.of(
                        TaskSummaryDto.builder()
                                .id(1L)
                                .title("Implement user registration")
                                .completed(true)
                                .build(),
                        TaskSummaryDto.builder()
                                .id(2L)
                                .title("Integrate GigaChat")
                                .completed(false)
                                .build()
                ))
                .build();

        String report = summaryGeneratorService.generate(request);

        assertThat(report).isNotBlank();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(GigaChatProperties.class)
    @Import({
            GigaChatConfig.class,
            GigaChatAuthClient.class,
            GigaChatTokenProvider.class,
            GigaChatClient.class,
            ReportPromptBuilder.class,
            SummaryGeneratorServiceImpl.class
    })
    static class TestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
