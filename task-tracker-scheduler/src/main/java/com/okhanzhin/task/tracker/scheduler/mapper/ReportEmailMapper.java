package com.okhanzhin.task.tracker.scheduler.mapper;

import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author okhanzhin on 16.10.2026
 */
@Component
public class ReportEmailMapper {

    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ZoneId reportZone;

    public ReportEmailMapper(
            @Value("${app.reports.daily.zone:UTC}") String reportZone
    ) {
        this.reportZone = ZoneId.of(reportZone);
    }

    public EmailSendingTask toEmailTask(
            SummarizationRequest request,
            String summary
    ) {
        Objects.requireNonNull(
                request,
                "request must not be null"
        );
        Objects.requireNonNull(
                request.getPeriodStart(),
                "periodStart must not be null"
        );
        Objects.requireNonNull(
                request.getPeriodEnd(),
                "periodEnd must not be null"
        );

        if (!StringUtils.hasText(request.getRequestId())) {
            throw new IllegalArgumentException(
                    "requestId must not be blank"
            );
        }

        if (!StringUtils.hasText(request.getUserEmail())) {
            throw new IllegalArgumentException(
                    "userEmail must not be blank"
            );
        }

        if (!StringUtils.hasText(summary)) {
            throw new IllegalArgumentException(
                    "summary must not be blank"
            );
        }

        if (!request.getPeriodStart().isBefore(request.getPeriodEnd())) {
            throw new IllegalArgumentException(
                    "periodStart must be before periodEnd"
            );
        }

        return EmailSendingTask.builder()
                .messageId(createMessageId(request.getRequestId()))
                .recipientEmail(request.getUserEmail())
                .subject(createSubject(request))
                .body(summary)
                .build();
    }

    private UUID createMessageId(String requestId) {
        String source = "daily-report-email:" + requestId;

        return UUID.nameUUIDFromBytes(
                source.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String createSubject(SummarizationRequest request) {
        return "Task report for period %s to %s (%s)".formatted(
                formatUtcDateTime(request.getPeriodStart()),
                formatUtcDateTime(request.getPeriodEnd()),
                reportZone.getId()
        );
    }

    private String formatUtcDateTime(LocalDateTime utcDateTime) {
        return utcDateTime
                .toInstant(ZoneOffset.UTC)
                .atZone(reportZone)
                .format(PERIOD_FORMAT);
    }
}
