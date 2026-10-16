package com.okhanzhin.task.tracker.scheduler.service;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletionException;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportJob {

    private final DailyReportRequestSource requestSource;
    private final UserReportDeliveryService deliveryService;

    public void run(Instant from, Instant to) {
        validatePeriod(from, to);

        log.info("Starting daily report job: from={}, to={}", from, to);

        requestSource.forEach(from, to, this::processUser);

        log.info("Finished daily report job: from={}, to={}", from, to);
    }

    private void processUser(SummarizationRequest request) {
        try {
            deliveryService.generateAndEnqueue(request).join();

            log.info("Report email command published: requestId={}, userId={}", request.getRequestId(), request.getUserId());
        } catch (CompletionException exception) {
            logFailure(
                    request,
                    exception.getCause() != null
                            ? exception.getCause()
                            : exception
            );
        } catch (RuntimeException exception) {
            logFailure(request, exception);
        }
    }

    private void logFailure(
            SummarizationRequest request,
            Throwable failure
    ) {
        log.error(
                "Failed to generate or enqueue report: requestId={}, userId={}",
                request.getRequestId(),
                request.getUserId(),
                failure
        );
    }

    private void validatePeriod(Instant from, Instant to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException(
                    "from must be before to"
            );
        }
    }
}
