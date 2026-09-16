package com.okhanzhin.task.tracker.backend.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Запрос на обновление данных задачи
 *
 * @author okhanzhin on 10.09.2026
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean completed;

    @AssertTrue(message = "At least one field must be provided")
    @JsonIgnore
    public boolean hasAtLeastOneField() {
        return title != null || description != null || completed != null;
    }

    @AssertTrue(message = "Title must not be blank")
    @JsonIgnore
    public boolean isTitleValid() {
        return title == null || !title.isBlank();
    }
}
