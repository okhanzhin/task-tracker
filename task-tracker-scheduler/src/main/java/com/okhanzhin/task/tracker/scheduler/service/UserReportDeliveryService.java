package com.okhanzhin.task.tracker.scheduler.service;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
public interface UserReportDeliveryService {

    CompletableFuture<Void> generateAndEnqueue(SummarizationRequest request);
}
