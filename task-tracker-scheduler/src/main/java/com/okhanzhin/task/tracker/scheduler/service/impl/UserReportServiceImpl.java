package com.okhanzhin.task.tracker.scheduler.service.impl;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationResponse;
import com.okhanzhin.task.tracker.scheduler.exception.SummarizationException;
import com.okhanzhin.task.tracker.scheduler.producer.SummarizationRequestProducer;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Service
@RequiredArgsConstructor
public class UserReportServiceImpl implements UserReportService {

    private final SummarizationRequestProducer producer;

    @Override
    public CompletableFuture<String> generate(SummarizationRequest request) {
        return producer
                .send(request)
                .thenApply(this::extractSummary);
    }

    private String extractSummary(SummarizationResponse response) {
        if (!response.isSuccess()) {
            throw new SummarizationException(
                    "Summarization failed: "
                            + response.getErrorMessage()
            );
        }

        if (!StringUtils.hasText(response.getSummary())) {
            throw new SummarizationException(
                    "Summarization service returned an empty summary"
            );
        }

        return response.getSummary();
    }
}
