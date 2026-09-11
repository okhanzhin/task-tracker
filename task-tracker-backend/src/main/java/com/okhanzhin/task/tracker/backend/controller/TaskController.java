package com.okhanzhin.task.tracker.backend.controller;

import com.okhanzhin.task.tracker.backend.model.dto.SecurityUser;
import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.UpdateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.TaskResponse;
import com.okhanzhin.task.tracker.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static com.okhanzhin.task.tracker.backend.utils.Constants.TASKS_BASE_URI;

/**
 *
 * @author okhanzhin on 09.09.2026
 */
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @RequestBody CreateTaskRequest request

    ) {
        TaskResponse response = taskService.createTask(principal.getUserId(), request);
        URI location = URI.create(TASKS_BASE_URI + "/" + response.getId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllUserTasks(
            @AuthenticationPrincipal SecurityUser principal
    ) {
        List<TaskResponse> tasks = taskService.getAllTasks(principal.getUserId());

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTask(
            @PathVariable("taskId") Long taskId,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        TaskResponse response = taskService.findByTaskIdAndOwnerId(taskId, principal.getUserId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable("taskId") Long taskId,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(taskId, principal.getUserId(), request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(
            @PathVariable("taskId") Long taskId,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        taskService.deleteByTaskIdAndOwnerId(taskId, principal.getUserId());

        return ResponseEntity.noContent().build();
    }
}
