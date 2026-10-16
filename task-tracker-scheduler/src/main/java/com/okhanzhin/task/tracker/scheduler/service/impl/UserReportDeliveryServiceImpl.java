package com.okhanzhin.task.tracker.scheduler.service.impl;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.scheduler.mapper.ReportEmailMapper;
import com.okhanzhin.task.tracker.scheduler.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.scheduler.service.UserReportDeliveryService;
import com.okhanzhin.task.tracker.scheduler.service.UserReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Service
@RequiredArgsConstructor
public class UserReportDeliveryServiceImpl implements UserReportDeliveryService {

    private final UserReportService userReportService;
    private final ReportEmailMapper reportEmailMapper;
    private final EmailTaskProducer emailTaskProducer;

    @Override
    public CompletableFuture<Void> generateAndEnqueue(SummarizationRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        return userReportService.generate(request)
                .thenApply(summary ->
                        reportEmailMapper.toEmailTask(request, summary)
                )
                .thenCompose(emailTaskProducer::send)
                .thenAccept(sendResult -> {});
    }
}
