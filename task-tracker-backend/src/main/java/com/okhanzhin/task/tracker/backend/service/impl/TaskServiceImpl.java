package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.exception.TaskNotFoundException;
import com.okhanzhin.task.tracker.backend.exception.UserNotFoundException;
import com.okhanzhin.task.tracker.backend.mapper.TaskMapper;
import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.UpdateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.TaskResponse;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.TaskRepository;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.backend.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author okhanzhin on 09.09.2026
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public TaskResponse createTask(Long ownerId, CreateTaskRequest request) {
        UserDAO owner = userRepository.findById(ownerId)
                .orElseThrow(UserNotFoundException::new);
        TaskDAO task = taskMapper.map(owner, request);
        TaskDAO savedTask = taskRepository.save(task);

        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse findByTaskIdAndOwnerId(Long taskId, Long ownerId) {
        TaskDAO task = taskRepository.findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(TaskNotFoundException::new);

        return taskMapper.toResponse(task);
    }

    @Override
    public List<TaskResponse> getAllTasks(Long ownerId) {
        UserDAO owner = userRepository.findById(ownerId)
                .orElseThrow(UserNotFoundException::new);
        List<TaskDAO> taskDAOList = taskRepository.findDistinctByOwner(owner);

        return taskDAOList.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, Long ownerId, UpdateTaskRequest request) {
        TaskDAO taskDAO = taskRepository.findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(TaskNotFoundException::new);

        if (request.getTitle() != null && !request.getTitle().equals(taskDAO.getTitle())) {
            taskDAO.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            taskDAO.setDescription(request.getDescription());
        }

        if (request.getCompleted() != null) {
            updateCompletionStatus(taskDAO, request.getCompleted());
        }

        taskRepository.save(taskDAO);

        return taskMapper.toResponse(taskDAO);
    }

    @Override
    @Transactional
    public void deleteByTaskIdAndOwnerId(Long taskId, Long ownerId) throws ApiException {
        TaskDAO task = taskRepository.findByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(TaskNotFoundException::new);

        taskRepository.delete(task);
    }

    private void updateCompletionStatus(TaskDAO task, boolean requestedCompleted) {
        if (!task.isCompleted() && requestedCompleted) {
            task.setCompleted(true);
            task.setCompletedAt(Instant.now(clock));
        }

        if (task.isCompleted() && !requestedCompleted) {
            task.setCompleted(false);
            task.setCompletedAt(null);
        }
    }
}
