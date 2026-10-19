package com.okhanzhin.summarization.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.summarization.service.service.ReportPromptBuilder;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author okhanzhin on 17.10.2026
 */
public class ReportPromptBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ReportPromptBuilder promptBuilder =
            new ReportPromptBuilder(objectMapper);

    @Test
    void shouldBuildReportDataWithoutPersonalAndMessagingFields()
            throws Exception {
        SummarizationRequest request = SummarizationRequest.builder()
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

        String prompt = promptBuilder.buildUserPrompt(request);
        JsonNode data = objectMapper.readTree(prompt);

        assertThat(data.path("completedCount").asInt()).isEqualTo(1);
        assertThat(data.path("incompleteCount").asInt()).isEqualTo(1);
        assertThat(data.path("totalCount").asInt()).isEqualTo(2);

        assertThat(data.path("completedTasks").get(0).asText())
                .isEqualTo("Implement registration");

        assertThat(data.path("incompleteTasks").get(0).asText())
                .isEqualTo("Integrate GigaChat");

        assertThat(prompt).doesNotContain(
                "user@example.com",
                "request-1",
                "summarization-response",
                "\"userId\"",
                "\"id\""
        );
    }

    @Test
    void shouldPreserveQuotesAndLineBreaksInTaskTitle() throws Exception {
        String title = "Review \"registration\"\nand validation";

        SummarizationRequest request = SummarizationRequest.builder()
                .periodStart(LocalDateTime.of(2026, 10, 16, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 10, 17, 0, 0))
                .tasks(List.of(task(1L, title, false)))
                .build();

        String prompt = promptBuilder.buildUserPrompt(request);
        JsonNode data = objectMapper.readTree(prompt);

        assertThat(data.path("incompleteTasks").get(0).asText())
                .isEqualTo(title);
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
