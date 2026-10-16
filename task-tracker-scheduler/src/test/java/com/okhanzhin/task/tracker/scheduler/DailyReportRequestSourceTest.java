package com.okhanzhin.task.tracker.scheduler;

import com.okhanzhin.task.tracker.common.model.messaging.summarization.SummarizationRequest;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
/**
 *
 * @author okhanzhin on 15.10.2026
 */
@ExtendWith(MockitoExtension.class)
public class DailyReportRequestSourceTest {

    private static final Instant FROM =
            Instant.parse("2026-10-15T00:00:00Z");

    private static final Instant TO =
            Instant.parse("2026-10-16T00:00:00Z");

    private static final int PAGE_SIZE = 100;

    @Mock
    private TaskTrackerApiClient apiClient;

    @Mock
    private TaskTrackerApiClientProperties properties;

    @Mock
    private SummarizationRequestMapper mapper;

    @Mock
    private Consumer<SummarizationRequest> consumer;

    private DailyReportRequestSource source;

    @BeforeEach
    void setUp() {
        source = new DailyReportRequestSource(
                apiClient,
                properties,
                mapper
        );
    }

    @Test
    void shouldProcessPagesAndPassCursorToNextRequest() {
        configurePageSize();

        ReportPeriod period = new ReportPeriod(FROM, TO);

        UserReportData firstUser = mock(UserReportData.class);
        UserReportData secondUser = mock(UserReportData.class);

        SummarizationRequest firstRequest = createRequest("request-1");
        SummarizationRequest secondRequest = createRequest("request-2");

        DailyReportDataPage firstPage = new DailyReportDataPage(
                period,
                List.of(firstUser),
                42L
        );

        DailyReportDataPage secondPage = new DailyReportDataPage(
                period,
                List.of(secondUser),
                null
        );

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        )).thenReturn(firstPage);

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, 42L
        )).thenReturn(secondPage);

        when(mapper.toRequest(period, firstUser))
                .thenReturn(firstRequest);
        when(mapper.toRequest(period, secondUser))
                .thenReturn(secondRequest);

        source.forEach(FROM, TO, consumer);

        InOrder order = inOrder(apiClient, mapper, consumer);

        order.verify(apiClient).getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        );
        order.verify(mapper).toRequest(period, firstUser);
        order.verify(consumer).accept(firstRequest);

        order.verify(apiClient).getDailyReportData(
                FROM, TO, PAGE_SIZE, 42L
        );
        order.verify(mapper).toRequest(period, secondUser);
        order.verify(consumer).accept(secondRequest);

        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopOnEmptyFinalPage() {
        configurePageSize();

        DailyReportDataPage page = new DailyReportDataPage(
                new ReportPeriod(FROM, TO),
                List.of(),
                null
        );

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        )).thenReturn(page);

        source.forEach(FROM, TO, consumer);

        verify(apiClient).getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        );
        verifyNoMoreInteractions(apiClient);
        verifyNoInteractions(mapper, consumer);
    }

    @ParameterizedTest
    @ValueSource(longs = {42L, 41L})
    void shouldRejectNonAdvancingCursorBeforeProcessingPage(
            long nextCursor
    ) {
        configurePageSize();

        ReportPeriod period = new ReportPeriod(FROM, TO);

        UserReportData firstUser = mock(UserReportData.class);
        UserReportData invalidPageUser = mock(UserReportData.class);

        SummarizationRequest firstRequest = createRequest("request-1");

        DailyReportDataPage firstPage = new DailyReportDataPage(
                period,
                List.of(firstUser),
                42L
        );

        DailyReportDataPage invalidPage = new DailyReportDataPage(
                period,
                List.of(invalidPageUser),
                nextCursor
        );

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        )).thenReturn(firstPage);

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, 42L
        )).thenReturn(invalidPage);

        when(mapper.toRequest(period, firstUser))
                .thenReturn(firstRequest);

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> source.forEach(FROM, TO, consumer)
        );

        assertEquals(
                "Task-tracker-backend returned "
                        + "a non-advancing report cursor",
                exception.getMessage()
        );

        verify(consumer).accept(firstRequest);
        verifyNoMoreInteractions(consumer);

        verify(mapper, never()).toRequest(
                period,
                invalidPageUser
        );

        verify(apiClient).getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        );
        verify(apiClient).getDailyReportData(
                FROM, TO, PAGE_SIZE, 42L
        );
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    void shouldRejectUnexpectedPeriodBeforeProcessingUsers() {
        configurePageSize();

        DailyReportDataPage page = new DailyReportDataPage(
                new ReportPeriod(FROM.minusSeconds(1), TO),
                List.of(mock(UserReportData.class)),
                null
        );

        when(apiClient.getDailyReportData(
                FROM, TO, PAGE_SIZE, null
        )).thenReturn(page);

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> source.forEach(FROM, TO, consumer)
        );

        assertEquals(
                "Task-tracker-backend returned a report page "
                        + "for an unexpected period",
                exception.getMessage()
        );

        verifyNoInteractions(mapper, consumer);
    }

    @Test
    void shouldRejectNullPage() {
        configurePageSize();

        TaskTrackerClientException exception = assertThrows(
                TaskTrackerClientException.class,
                () -> source.forEach(FROM, TO, consumer)
        );

        assertEquals(
                "Task-tracker-backend returned an invalid report page",
                exception.getMessage()
        );

        verifyNoInteractions(mapper, consumer);
    }

    @Test
    void shouldRejectInvalidInputPeriodBeforeCallingBackend() {
        assertThrows(
                IllegalArgumentException.class,
                () -> source.forEach(TO, FROM, consumer)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> source.forEach(FROM, FROM, consumer)
        );

        verifyNoInteractions(
                apiClient,
                properties,
                mapper,
                consumer
        );
    }

    private void configurePageSize() {
        when(properties.getDailyReportDataPageSize())
                .thenReturn(PAGE_SIZE);
    }

    private SummarizationRequest createRequest(String requestId) {
        return SummarizationRequest.builder()
                .requestId(requestId)
                .build();
    }
}
