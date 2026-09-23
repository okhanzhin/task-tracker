package com.okhanzhin.task.tracker.backend.utils;

import lombok.experimental.UtilityClass;

/**
 * Константы
 *
 * @author okhanzhin on 31.08.2026
 */
@UtilityClass
public class Constants {

    public static final String UK_USERS_EMAIL = "users_email_key";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String TASKS_BASE_URI = "/api/v1/tasks";

    public static final String SUCCESS_USER_REGISTRATION_EMAIL_TASK_BODY = "Congratulations! Your Task Tracker account has been successfully created—start organizing your tasks and achieving more today.";
    public static final String SUCCESS_USER_REGISTRATION_EMAIL_TASK_SUBJECT = "Task Tracker Service: Successfully Registration!";
}
