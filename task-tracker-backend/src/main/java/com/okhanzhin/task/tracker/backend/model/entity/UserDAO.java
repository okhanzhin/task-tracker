package com.okhanzhin.task.tracker.backend.model.entity;

import com.okhanzhin.task.tracker.backend.model.common.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 * @author okhanzhin on 30.08.2026
 */
@Entity
@Table(name = "users", schema = "tasks_tracker")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<TaskDAO> tasks;
}
