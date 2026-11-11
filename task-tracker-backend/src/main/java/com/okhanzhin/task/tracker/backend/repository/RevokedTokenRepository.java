package com.okhanzhin.task.tracker.backend.repository;

import com.okhanzhin.task.tracker.backend.model.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    @Modifying
    @Transactional
    @Query("""
            delete from RevokedToken token
            where token.expiresAt <= :now
            """)
    int deleteExpired(@Param("now") Instant now);
}

