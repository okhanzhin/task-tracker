package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.utils.TestDataFactory;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.common.model.report.TaskData;
import com.okhanzhin.task.tracker.common.model.report.UserReportData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;

import static com.okhanzhin.task.tracker.backend.utils.TestConstants.FROM;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.PASSWORD;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.REPORT_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TIMEZONE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TO;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
public class InternalReportTest extends IntegrationTest {

    @Test
    void shouldReturnEmptyPageWhenDatabaseIsEmpty() throws Exception {
        DailyReportDataPage page = requestPage(100, null);

        assertEquals(FROM, page.getPeriod().getFrom());
        assertEquals(TO, page.getPeriod().getTo());
        assertNotNull(page.getItems());
        assertTrue(page.getItems().isEmpty());
        assertNull(page.getNextAfterUserId());
    }

    @Test
    void shouldReturnOnlyEligibleUsersAndGroupTheirTasks()
            throws Exception {
        UserDAO mixedUser = createUser("mixed");
        UserDAO pendingUser = createUser("pending");
        UserDAO oldCompletedUser = createUser("old");
        createUser("empty");

        TaskDAO completedTask = createTask(
                mixedUser,
                "Completed during period",
                "Completed description",
                FROM.plusSeconds(3600)
        );

        TaskDAO mixedPendingTask = createTask(
                mixedUser,
                "Pending task",
                null,
                null
        );

        createTask(
                mixedUser,
                "Completed before period",
                null,
                FROM.minusSeconds(1)
        );

        createTask(
                mixedUser,
                "Completed after period",
                null,
                TO.plusSeconds(1)
        );

        TaskDAO pendingTask = createTask(
                pendingUser,
                "Another user's pending task",
                null,
                null
        );

        createTask(
                oldCompletedUser,
                "Old completed task",
                null,
                FROM.minusSeconds(3600)
        );

        DailyReportDataPage page = requestPage(100, null);

        assertEquals(
                List.of(mixedUser.getId(), pendingUser.getId()),
                TestDataFactory.userIds(page)
        );
        assertNull(page.getNextAfterUserId());

        UserReportData mixedReport = page.getItems().get(0);

        assertEquals(
                mixedUser.getEmail(),
                mixedReport.getUserEmail()
        );
        assertEquals(
                List.of(completedTask.getId()),
                TestDataFactory.taskIds(mixedReport.getCompletedTasks())
        );
        assertEquals(
                List.of(mixedPendingTask.getId()),
                TestDataFactory.taskIds(mixedReport.getPendingTasks())
        );

        TaskData completedData = mixedReport.getCompletedTasks().get(0);

        assertEquals(
                completedTask.getTitle(),
                completedData.getTitle()
        );
        assertEquals(
                completedTask.getDescription(),
                completedData.getDescription()
        );
        assertEquals(
                FROM.plusSeconds(3600),
                completedData.getCompletedAt()
        );

        TaskData pendingData = mixedReport.getPendingTasks().get(0);

        assertNull(pendingData.getDescription());
        assertNull(pendingData.getCompletedAt());

        UserReportData pendingReport = page.getItems().get(1);

        assertEquals(
                pendingUser.getEmail(),
                pendingReport.getUserEmail()
        );
        assertTrue(pendingReport.getCompletedTasks().isEmpty());
        assertEquals(
                List.of(pendingTask.getId()),
                TestDataFactory.taskIds(pendingReport.getPendingTasks())
        );
    }

    @Test
    void shouldIncludeFromBoundaryAndExcludeToBoundary()
            throws Exception {
        UserDAO user = createUser("boundaries");

        createTask(
                user,
                "Before from",
                null,
                FROM.minusSeconds(1)
        );

        TaskDAO atFrom = createTask(
                user,
                "Exactly at from",
                null,
                FROM
        );

        TaskDAO beforeTo = createTask(
                user,
                "Before to",
                null,
                TO.minusSeconds(1)
        );

        createTask(
                user,
                "Exactly at to",
                null,
                TO
        );

        DailyReportDataPage page = requestPage(100, null);

        assertEquals(1, page.getItems().size());

        UserReportData report = page.getItems().get(0);

        assertEquals(
                List.of(atFrom.getId(), beforeTo.getId()),
                TestDataFactory.taskIds(report.getCompletedTasks())
        );
        assertTrue(report.getPendingTasks().isEmpty());
    }

    @Test
    void shouldExcludeUserWhoseOnlyTaskWasCompletedAtTo()
            throws Exception {
        UserDAO user = createUser("only-at-to");

        createTask(user, "Exactly at to", null, TO);

        DailyReportDataPage page = requestPage(100, null);

        assertTrue(page.getItems().isEmpty());
        assertNull(page.getNextAfterUserId());
    }

    @Test
    void shouldReturnReopenedTaskAsPending() throws Exception {
        UserDAO user = createUser("reopened");

        TaskDAO task = createTask(
                user,
                "Reopened task",
                null,
                FROM.plusSeconds(3600)
        );

        task.setCompleted(false);
        task.setCompletedAt(null);
        taskRepository.saveAndFlush(task);

        DailyReportDataPage page = requestPage(100, null);

        assertEquals(1, page.getItems().size());

        UserReportData report = page.getItems().get(0);

        assertTrue(report.getCompletedTasks().isEmpty());
        assertEquals(
                List.of(task.getId()),
                TestDataFactory.taskIds(report.getPendingTasks())
        );
        assertNull(
                report.getPendingTasks().get(0).getCompletedAt()
        );
    }

    @Test
    void shouldPaginateEligibleUsersWithoutSkippingExtraCandidate()
            throws Exception {
        UserDAO first = createUser("first");
        createTask(first, "First task", null, null);

        UserDAO excluded = createUser("excluded");
        createTask(
                excluded,
                "Old task",
                null,
                FROM.minusSeconds(1)
        );

        UserDAO second = createUser("second");
        createTask(second, "Second task", null, null);

        UserDAO third = createUser("third");
        createTask(third, "Third task", null, null);

        DailyReportDataPage firstPage = requestPage(2, null);

        assertEquals(
                List.of(first.getId(), second.getId()),
                TestDataFactory.userIds(firstPage)
        );
        assertEquals(
                second.getId(),
                firstPage.getNextAfterUserId()
        );

        DailyReportDataPage secondPage = requestPage(
                2,
                firstPage.getNextAfterUserId()
        );

        assertEquals(
                List.of(third.getId()),
                TestDataFactory.userIds(secondPage)
        );
        assertNull(secondPage.getNextAfterUserId());
    }

    @Test
    void shouldReturnNullCursorWhenUserCountEqualsLimit()
            throws Exception {
        UserDAO first = createUser("exact-first");
        createTask(first, "First task", null, null);

        UserDAO second = createUser("exact-second");
        createTask(second, "Second task", null, null);

        DailyReportDataPage page = requestPage(2, null);

        assertEquals(
                List.of(first.getId(), second.getId()),
                TestDataFactory.userIds(page)
        );
        assertNull(page.getNextAfterUserId());
    }

    @Test
    void shouldReturnEmptyPageAfterLastUser() throws Exception {
        UserDAO user = createUser("last");
        createTask(user, "Pending task", null, null);

        DailyReportDataPage page = requestPage(
                100,
                user.getId()
        );

        assertTrue(page.getItems().isEmpty());
        assertNull(page.getNextAfterUserId());
    }

    @Test
    void shouldNotLimitTasksByUserPageSize() throws Exception {
        UserDAO user = createUser("many-tasks");

        for (int i = 0; i < 5; i++) {
            createTask(
                    user,
                    "Pending " + i,
                    null,
                    null
            );
        }

        for (int i = 0; i < 3; i++) {
            createTask(
                    user,
                    "Completed " + i,
                    null,
                    FROM.plusSeconds(i + 1)
            );
        }

        DailyReportDataPage page = requestPage(1, null);

        assertEquals(1, page.getItems().size());

        UserReportData report = page.getItems().get(0);

        assertEquals(5, report.getPendingTasks().size());
        assertEquals(3, report.getCompletedTasks().size());
        assertNull(page.getNextAfterUserId());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 501})
    void shouldRejectInvalidLimit(int limit) throws Exception {
        mockMvc.perform(validRequest()
                        .param("limit", Integer.toString(limit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        not(emptyOrNullString())
                ));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void shouldRejectInvalidCursor(long cursor) throws Exception {
        mockMvc.perform(validRequest()
                        .param("afterUserId", Long.toString(cursor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        not(emptyOrNullString())
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-10-06T00:00:00Z",
            "2026-10-05T00:00:00Z"
    })
    void shouldRejectEqualOrReversedPeriod(String to)
            throws Exception {
        mockMvc.perform(authorizedRequest()
                        .param("from", FROM.toString())
                        .param("to", to))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        not(emptyOrNullString())
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"from", "to"})
    void shouldRejectMissingPeriodParameter(String missing)
            throws Exception {
        MockHttpServletRequestBuilder request = authorizedRequest();

        if (!missing.equals("from")) {
            request.param("from", FROM.toString());
        }

        if (!missing.equals("to")) {
            request.param("to", TO.toString());
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        not(emptyOrNullString())
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"from", "to", "limit", "afterUserId"})
    void shouldRejectInvalidParameterFormat(String parameter)
            throws Exception {
        MockHttpServletRequestBuilder request = authorizedRequest()
                .param(
                        "from",
                        parameter.equals("from")
                                ? "invalid"
                                : FROM.toString()
                )
                .param(
                        "to",
                        parameter.equals("to")
                                ? "invalid"
                                : TO.toString()
                );

        if (parameter.equals("limit")) {
            request.param("limit", "invalid");
        }

        if (parameter.equals("afterUserId")) {
            request.param("afterUserId", "invalid");
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath(
                        "$.message",
                        not(emptyOrNullString())
                ));
    }

    @Test
    void shouldUseDefaultLimitWhenNotProvided() throws Exception {
        UserDAO user = createUser("default-limit");
        createTask(user, "Pending task", null, null);

        mockMvc.perform(validRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    private UserDAO createUser(String name) {
        return TestDataFactory.savedUser(
                name,
                name + "@example.com",
                PASSWORD,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );
    }

    private TaskDAO createTask(
            UserDAO owner,
            String title,
            String description,
            Instant completedAt
    ) {
        return TestDataFactory.savedTask(
                title,
                description,
                completedAt,
                owner,
                taskRepository
        );
    }

    private MockHttpServletRequestBuilder authorizedRequest() {
        return get(REPORT_URL)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtService.generateSchedulerToken()
                )
                .accept(MediaType.APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder validRequest() {
        return authorizedRequest()
                .param("from", FROM.toString())
                .param("to", TO.toString());
    }

    private DailyReportDataPage requestPage(
            int limit,
            Long afterUserId
    ) throws Exception {
        MockHttpServletRequestBuilder request = validRequest()
                .param("limit", Integer.toString(limit));

        if (afterUserId != null) {
            request.param(
                    "afterUserId",
                    afterUserId.toString()
            );
        }

        String body = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(
                body,
                DailyReportDataPage.class
        );
    }
}
