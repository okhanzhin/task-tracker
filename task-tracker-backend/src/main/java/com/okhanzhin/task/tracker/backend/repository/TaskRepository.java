package com.okhanzhin.task.tracker.backend.repository;

import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
