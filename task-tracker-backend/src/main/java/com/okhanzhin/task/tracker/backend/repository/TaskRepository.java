package com.okhanzhin.task.tracker.backend.repository;

import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.model.entity.projection.ReportTaskView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author okhanzhin on 10.09.2026
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskDAO, Long> {

    Optional<TaskDAO> findByIdAndOwnerId(Long taskId, Long ownerId);

    List<TaskDAO> findDistinctByOwner(UserDAO owner);

    @Query("""
            select
                task.owner.id as ownerId,
                task.id as id,
                task.title as title,
                task.description as description,
                task.completed as completed,
                task.completedAt as completedAt
            from TaskDAO task
            where task.owner.id in :userIds
                 and (
                    task.completed = false
                    or (
                        task.completed = true
                        and task.completedAt >= :from
                        and task.completedAt < :to
                    )
                 )
            order by task.owner.id asc, task.id asc
            """)
    List<ReportTaskView> findReportTasks(
            @Param("userIds") Collection<Long> userIds,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
