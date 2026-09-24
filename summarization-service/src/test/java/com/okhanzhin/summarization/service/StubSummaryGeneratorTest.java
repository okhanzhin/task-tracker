package com.okhanzhin.summarization.service;

import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.request.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.request.TaskSummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@SpringBootTest
@ActiveProfiles("test")
public class StubSummaryGeneratorTest {

    @Autowired
    private SummaryGeneratorService summaryGeneratorService;

    @Test
    void shouldGenerateSummaryWithCompletedAndUncompletedTasksCount() {
        SummarizationRequest request = SummarizationRequest.builder()
                .requestId("request-1")
                .userId(1L)
                .periodStart(LocalDateTime.of(2026, 9, 23, 0, 0))
                .periodEnd(LocalDateTime.of(2026, 9, 24, 0, 0))
                .tasks(List.of(
                        TaskSummaryDto.builder()
                                .id(1L)
                                .title("Completed task")
                                .completed(true)
                                .build(),
                        TaskSummaryDto.builder()
                                .id(2L)
                                .title("Uncompleted task")
                                .completed(false)
                                .build()
                ))
                .build();

        String summary = summaryGeneratorService.generate(request);

        assertThat(summary)
                .contains("You completed 1 task(s).")
                .contains("You still have 1 incomplete task(s).")
                .contains("Total tasks included in this report: 2.");
    }
}
