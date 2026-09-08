package com.okhanzhin.task.tracker.backend.repository;

import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Repository
public interface UserRepository extends JpaRepository<UserDAO, Long> {

    Optional<UserDAO> findByEmail(String email);
}
