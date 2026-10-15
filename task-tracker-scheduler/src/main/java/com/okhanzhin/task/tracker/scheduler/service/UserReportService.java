package com.okhanzhin.task.tracker.scheduler.service;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
public interface UserReportService {

    CompletableFuture<String> generate(SummarizationRequest request);
}
