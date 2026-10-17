package com.okhanzhin.summarization.service.service.impl;

import com.okhanzhin.summarization.service.client.GigaChatClient;
import com.okhanzhin.summarization.service.service.ReportPromptBuilder;
import com.okhanzhin.summarization.service.service.SummaryGeneratorService;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author okhanzhin on 24.09.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SummaryGeneratorServiceImpl implements SummaryGeneratorService {

    private final ReportPromptBuilder promptBuilder;
    private final GigaChatClient gigaChatClient;

    @Override
    public String generate(SummarizationRequest request) {
        String userPrompt = promptBuilder.buildUserPrompt(request);
        String systemPrompt = promptBuilder.buildSystemPrompt();

        String summary = gigaChatClient.generate(systemPrompt, userPrompt);

        log.info(
                "Generated daily summary: requestId={}, userId={}, taskCount={}",
                request.getRequestId(),
                request.getUserId(),
                request.getTasks().size()
        );

        return summary;
    }
}
