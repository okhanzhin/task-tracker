package com.okhanzhin.summarization.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.summarization.service.client.GigaChatClient;
import com.okhanzhin.summarization.service.exception.GigaChatClientException;
import com.okhanzhin.summarization.service.service.ReportPromptBuilder;
import com.okhanzhin.summarization.service.service.impl.SummaryGeneratorServiceImpl;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
public class SummaryGeneratorServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GigaChatClient gigaChatClient;
    private ReportPromptBuilder promptBuilder;
    private SummaryGeneratorServiceImpl service;

    @BeforeEach
    void setUp() {
        gigaChatClient = mock(GigaChatClient.class);
        promptBuilder = new ReportPromptBuilder(objectMapper);

        service = new SummaryGeneratorServiceImpl(
                promptBuilder,
                gigaChatClient
        );
    }

    @Test
    void shouldGenerateReportUsingPreparedPrompts() throws Exception {
        SummarizationRequest request = validRequest();

        String expectedReport = """
                Report period
                2026-10-16T00:00 inclusive to 2026-10-17T00:00 exclusive

                Completed tasks
                Implement registration

                Incomplete tasks
                Integrate GigaChat

                Summary
                Completed: 1
                Incomplete: 1
                Total: 2
                """;

        when(gigaChatClient.generate(anyString(), anyString()))
                .thenReturn(expectedReport);

        String result = service.generate(request);

        assertThat(result).isEqualTo(expectedReport);

        ArgumentCaptor<String> systemPromptCaptor =
                ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> userPromptCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(gigaChatClient).generate(
                systemPromptCaptor.capture(),
                userPromptCaptor.capture()
        );

        assertThat(systemPromptCaptor.getValue())
                .isEqualTo(promptBuilder.buildSystemPrompt());

        JsonNode data = objectMapper.readTree(
                userPromptCaptor.getValue()
        );

        assertThat(data.path("completedCount").asInt()).isEqualTo(1);
        assertThat(data.path("incompleteCount").asInt()).isEqualTo(1);
        assertThat(data.path("totalCount").asInt()).isEqualTo(2);

        assertThat(data.path("completedTasks").get(0).asText())
                .isEqualTo("Implement registration");

        assertThat(data.path("incompleteTasks").get(0).asText())
                .isEqualTo("Integrate GigaChat");

        assertThat(data.has("userEmail")).isFalse();
        assertThat(data.has("userId")).isFalse();
        assertThat(data.has("requestId")).isFalse();
        assertThat(data.has("replyTopic")).isFalse();
    }

    @Test
    void shouldPropagateGenerationFailure() {
        GigaChatClientException failure = new GigaChatClientException(
                "GigaChat generation request failed with HTTP status 503"
        );

        when(gigaChatClient.generate(anyString(), anyString()))
                .thenThrow(failure);

        assertThatThrownBy(() -> service.generate(validRequest()))
                .isSameAs(failure);
    }

    @Test
    void shouldNotCallGigaChatWhenPeriodIsInvalid() {
        SummarizationRequest request = SummarizationRequest.builder()
                .periodStart(LocalDateTime.of(2026, 10, 17, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 16, 0, 0))
                .tasks(List.of(task(1L, "Implement registration", true)))
                .build();

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Period start must be before period end");

        verifyNoInteractions(gigaChatClient);
    }

    @Test
    void shouldNotCallGigaChatWhenTaskTitleIsBlank() {
        SummarizationRequest request = SummarizationRequest.builder()
                .periodStart(LocalDateTime.of(2026, 10, 16, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 17, 0, 0))
                .tasks(List.of(task(1L, " ", false)))
                .build();

        assertThatThrownBy(() -> service.generate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title must not be blank");

        verifyNoInteractions(gigaChatClient);
    }

    private SummarizationRequest validRequest() {
        return SummarizationRequest.builder()
                .requestId("request-1")
                .userId(42L)
                .userEmail("user@example.com")
                .periodStart(LocalDateTime.of(2026, 10, 16, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 17, 0, 0))
                .replyTopic("summarization-response")
                .tasks(List.of(
                        task(1L, "Implement registration", true),
                        task(2L, "Integrate GigaChat", false)
                ))
                .build();
    }

    private TaskSummaryDto task(
            Long id,
            String title,
            boolean completed
    ) {
        return TaskSummaryDto.builder()
                .id(id)
                .title(title)
                .completed(completed)
                .build();
    }
}
