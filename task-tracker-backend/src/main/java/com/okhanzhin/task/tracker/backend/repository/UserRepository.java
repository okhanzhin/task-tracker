package com.okhanzhin.task.tracker.backend.repository;

import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.model.entity.projection.ReportUserView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Repository
public interface UserRepository extends JpaRepository<UserDAO, Long> {

    Optional<UserDAO> findByEmail(String email);

    @Query("""
            select
                user.id as  id,
                user.email as email
            from UserDAO user
            where (:afterUserId is null or user.id > :afterUserId)
                and exists(
                    select task.id
                    from TaskDAO task
                    where task.owner.id = user.id
                        and (
                            task.completed = false
                            or (
                                task.completed = true
                                and task.completedAt >= :from
                                and task.completedAt < :to
                            )
                        )
                )
            order by user.id asc
            """)
    List<ReportUserView> findReportUsers(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("afterUserId") Long afterUserId,
            Pageable pageable
    );
}
