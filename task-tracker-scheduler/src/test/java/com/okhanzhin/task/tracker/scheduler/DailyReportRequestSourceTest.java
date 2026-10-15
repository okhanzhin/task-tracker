package com.okhanzhin.task.tracker.scheduler;

import com.github.dockerjava.api.model.Task;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
import com.okhanzhin.task.tracker.common.model.messaging.summarization.TaskSummaryDto;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.common.model.report.ReportPeriod;
import com.okhanzhin.task.tracker.common.model.report.UserReportData;
import com.okhanzhin.task.tracker.scheduler.client.TaskTrackerApiClient;
import com.okhanzhin.task.tracker.scheduler.config.properties.TaskTrackerApiClientProperties;
import com.okhanzhin.task.tracker.scheduler.exception.TaskTrackerClientException;
import com.okhanzhin.task.tracker.scheduler.mapper.SummarizationRequestMapper;
import com.okhanzhin.task.tracker.scheduler.service.DailyReportRequestSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
/**
 *
 * @author okhanzhin on 15.10.2026
 */
@ExtendWith(MockitoExtension.class)
public class DailyReportRequestSourceTest {

    private static final Instant FROM =
            Instant.parse("2026-10-14T00:00:00Z");

    private static final Instant TO =
            Instant.parse("2026-10-15T00:00:00Z");

    private static final int PAGE_SIZE = 100;

    @Mock
    private TaskTrackerApiClient trackerApiClient;

    @Mock
    private SummarizationRequestMapper mapper;

    @Mock
    private TaskTrackerApiClientProperties properties;

    @InjectMocks
    private DailyReportRequestSource requestSource;

    @BeforeEach
    void setUp() {
        when(properties.getDailyReportDataPageSize())
                .thenReturn(PAGE_SIZE);
    }

    @Test
    void shouldReadAllPagesAndPassRequestsToConsumerInOrder() {
        ReportPeriod period = period(FROM, TO);

        UserReportData firstUser = user(10L, "first@example.com");
        UserReportData secondUser = user(20L, "second@example.com");
        UserReportData thirdUser = user(30L, "third@example.com");

        DailyReportDataPage firstPage = page(
                period,
                List.of(firstUser, secondUser),
                20L
        );

        DailyReportDataPage secondPage = page(
                period,
                List.of(thirdUser),
                null
        );

        SummarizationRequest firstRequest =
                request("request-10", firstUser);

        SummarizationRequest secondRequest =
                request("request-20", secondUser);

        SummarizationRequest thirdRequest =
                request("request-30", thirdUser);

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(firstPage);

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                20L
        )).thenReturn(secondPage);

        when(mapper.toRequest(period, firstUser))
                .thenReturn(firstRequest);

        when(mapper.toRequest(period, secondUser))
                .thenReturn(secondRequest);

        when(mapper.toRequest(period, thirdUser))
                .thenReturn(thirdRequest);

        List<SummarizationRequest> receivedRequests =
                new ArrayList<>();

        requestSource.forEach(
                FROM,
                TO,
                receivedRequests::add
        );

        assertEquals(
                List.of(
                        firstRequest,
                        secondRequest,
                        thirdRequest
                ),
                receivedRequests
        );

        InOrder clientOrder = inOrder(trackerApiClient);

        clientOrder.verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        null
                );

        clientOrder.verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        20L
                );

        InOrder mapperOrder = inOrder(mapper);

        mapperOrder.verify(mapper)
                .toRequest(period, firstUser);

        mapperOrder.verify(mapper)
                .toRequest(period, secondUser);

        mapperOrder.verify(mapper)
                .toRequest(period, thirdUser);

        verifyNoMoreInteractions(trackerApiClient, mapper);
    }

    @Test
    void shouldStopAfterFirstPageWhenNextCursorIsNull() {
        ReportPeriod period = period(FROM, TO);

        UserReportData user =
                user(10L, "user@example.com");

        DailyReportDataPage page = page(
                period,
                List.of(user),
                null
        );

        SummarizationRequest request =
                request("request-10", user);

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(page);

        when(mapper.toRequest(period, user))
                .thenReturn(request);

        List<SummarizationRequest> receivedRequests =
                new ArrayList<>();

        requestSource.forEach(
                FROM,
                TO,
                receivedRequests::add
        );

        assertEquals(List.of(request), receivedRequests);

        verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        null
                );

        verify(mapper).toRequest(period, user);

        verifyNoMoreInteractions(trackerApiClient, mapper);
    }

    @Test
    void shouldHandleEmptyPage() {
        ReportPeriod period = period(FROM, TO);

        DailyReportDataPage emptyPage = page(
                period,
                List.of(),
                null
        );

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(emptyPage);

        List<SummarizationRequest> receivedRequests =
                new ArrayList<>();

        requestSource.forEach(
                FROM,
                TO,
                receivedRequests::add
        );

        assertTrue(receivedRequests.isEmpty());

        verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        null
                );

        verifyNoMoreInteractions(trackerApiClient);

        verify(mapper, never())
                .toRequest(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectPageWithUnexpectedPeriod() {
        Instant unexpectedFrom =
                FROM.minusSeconds(60);

        ReportPeriod unexpectedPeriod =
                period(unexpectedFrom, TO);

        DailyReportDataPage page = page(
                unexpectedPeriod,
                List.of(),
                null
        );

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(page);

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> requestSource.forEach(
                        FROM,
                        TO,
                        request -> {
                        }
                )
        );

        assertTrue(
                exception.getMessage().contains("period"),
                () -> "Unexpected exception message: "
                        + exception.getMessage()
        );

        verify(mapper, never())
                .toRequest(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectCursorThatDoesNotAdvance() {
        ReportPeriod period = period(FROM, TO);

        DailyReportDataPage firstPage = page(
                period,
                List.of(),
                20L
        );

        DailyReportDataPage secondPage = page(
                period,
                List.of(),
                20L
        );

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(firstPage);

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                20L
        )).thenReturn(secondPage);

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> requestSource.forEach(
                        FROM,
                        TO,
                        request -> {
                        }
                )
        );

        assertTrue(
                exception.getMessage().contains("cursor"),
                () -> "Unexpected exception message: "
                        + exception.getMessage()
        );

        verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        null
                );

        verify(trackerApiClient)
                .getDailyReportData(
                        FROM,
                        TO,
                        PAGE_SIZE,
                        20L
                );

        verifyNoMoreInteractions(trackerApiClient);

        verify(mapper, never())
                .toRequest(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldRejectCursorThatMovesBackwards() {
        ReportPeriod period = period(FROM, TO);

        DailyReportDataPage firstPage = page(
                period,
                List.of(),
                20L
        );

        DailyReportDataPage secondPage = page(
                period,
                List.of(),
                10L
        );

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenReturn(firstPage);

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                20L
        )).thenReturn(secondPage);

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> requestSource.forEach(
                        FROM,
                        TO,
                        request -> {
                        }
                )
        );

        assertTrue(
                exception.getMessage().contains("cursor"),
                () -> "Unexpected exception message: "
                        + exception.getMessage()
        );

        verify(mapper, never())
                .toRequest(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void shouldPropagateTrackerApiClientException() {
        TaskTrackerClientException clientException =
                new TaskTrackerClientException(
                        "Backend is unavailable"
                );

        when(trackerApiClient.getDailyReportData(
                FROM,
                TO,
                PAGE_SIZE,
                null
        )).thenThrow(clientException);

        TaskTrackerClientException actualException = assertThrows(
                TaskTrackerClientException.class,
                () -> requestSource.forEach(
                        FROM,
                        TO,
                        request -> {
                        }
                )
        );

        assertEquals(clientException, actualException);

        verify(mapper, never())
                .toRequest(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    private static DailyReportDataPage page(
            ReportPeriod period,
            List<UserReportData> items,
            Long nextAfterUserId
    ) {
        return new DailyReportDataPage(
                period,
                items,
                nextAfterUserId
        );
    }

    private static ReportPeriod period(
            Instant from,
            Instant to
    ) {
        return new ReportPeriod(from, to);
    }

    private static UserReportData user(
            Long userId,
            String email
    ) {
        return new UserReportData(
                userId,
                email,
                List.of(),
                List.of()
        );
    }

    private static SummarizationRequest request(
            String requestId,
            UserReportData user
    ) {
        return SummarizationRequest.builder()
                .requestId(requestId)
                .userId(user.getUserId())
                .userEmail(user.getUserEmail())
                .periodStart(
                        LocalDateTime.ofInstant(
                                FROM,
                                ZoneOffset.UTC
                        )
                )
                .periodEnd(
                        LocalDateTime.ofInstant(
                                TO,
                                ZoneOffset.UTC
                        )
                )
                .tasks(
                        List.of(
                                TaskSummaryDto.builder()
                                        .id(1L)
                                        .title("Test task")
                                        .completed(false)
                                        .build()
                        )
                )
                .replyTopic("SUMMARIZATION_RESPONSES")
                .build();
    }
}
