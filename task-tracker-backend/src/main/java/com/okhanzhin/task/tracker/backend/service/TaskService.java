package com.okhanzhin.task.tracker.backend.service;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.UpdateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.TaskResponse;

import java.util.List;

/**
 *
 * @author okhanzhin on 09.09.2026
 */
public interface TaskService {

    TaskResponse createTask(Long ownerId, CreateTaskRequest request) throws ApiException;

    TaskResponse findByTaskIdAndOwnerId(Long taskId, Long ownerId) throws ApiException;

    List<TaskResponse> getAllTasks(Long ownerId) throws ApiException;

    TaskResponse updateTask(Long taskId, Long ownerId, UpdateTaskRequest request) throws ApiException;

    void deleteByTaskIdAndOwnerId(Long taskId, Long ownerId) throws ApiException;

}
