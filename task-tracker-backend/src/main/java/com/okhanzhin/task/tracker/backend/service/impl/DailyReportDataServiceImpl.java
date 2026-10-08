package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.mapper.TaskMapper;
import com.okhanzhin.task.tracker.backend.model.entity.projection.ReportTaskView;
import com.okhanzhin.task.tracker.backend.model.entity.projection.ReportUserView;
import com.okhanzhin.task.tracker.backend.repository.TaskRepository;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.backend.service.DailyReportDataService;
import com.okhanzhin.task.tracker.common.model.report.DailyReportDataPage;
import com.okhanzhin.task.tracker.common.model.report.ReportPeriod;
import com.okhanzhin.task.tracker.common.model.report.TaskData;
import com.okhanzhin.task.tracker.common.model.report.UserReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.okhanzhin.task.tracker.backend.utils.ReportRequestValidator.*;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
@Service
@RequiredArgsConstructor
public class DailyReportDataServiceImpl implements DailyReportDataService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public DailyReportDataPage getDailyReportData(
            Instant from,
            Instant to,
            int limit,
            Long afterUserId
    ) {
        validateRequest(from, to, limit, afterUserId);

        List<ReportUserView> candidates = userRepository.findReportUsers(
                from,
                to,
                afterUserId,
                PageRequest.of(0, limit + 1)
        );

        if (candidates.isEmpty()) {
            return new DailyReportDataPage(
                    new ReportPeriod(from, to),
                    new ArrayList<>(),
                    null
            );
        }

        boolean hasNext = candidates.size() > limit;

        List<ReportUserView> selectedUsers = candidates.subList(0, Math.min(candidates.size(), limit));
        Map<Long, UserReportData> reportsByUser = createUserReports(selectedUsers);

        List<Long> userIds = new ArrayList<>(reportsByUser.keySet());

        List<ReportTaskView> tasks = taskRepository.findReportTasks(userIds, from, to);

        distributeTasks(tasks, reportsByUser);

        Long nextAfterUserId = hasNext ? selectedUsers.getLast().getId() : null;

        return new DailyReportDataPage(
                new ReportPeriod(from, to),
                new ArrayList<>(reportsByUser.values()),
                nextAfterUserId
        );
    }

    private Map<Long, UserReportData> createUserReports(List<ReportUserView> selectedUsers) {
        Map<Long, UserReportData> reportsByUser = new LinkedHashMap<>();

        for (ReportUserView user: selectedUsers) {
            UserReportData report = new UserReportData(
                    user.getId(),
                    user.getEmail(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );

            reportsByUser.put(user.getId(), report);
        }

        return reportsByUser;
    }

    private void distributeTasks(
            List<ReportTaskView> tasks,
            Map<Long, UserReportData> reportsByUser
    ) {
        for (ReportTaskView task : tasks) {
            UserReportData report = reportsByUser.get(task.getOwnerId());

            if (report == null) {
                throw new IllegalStateException("Task returned for an unselected user");
            }

            TaskData taskData = taskMapper.toTaskData(task);

            if (Boolean.TRUE.equals(task.getCompleted())) {
                report.getCompletedTasks().add(taskData);
            } else {
                report.getPendingTasks().add(taskData);
            }
        }
    }
}
