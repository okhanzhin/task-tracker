package com.okhanzhin.task.tracker.backend.utils;

import lombok.experimental.UtilityClass;

/**
 *
 * @author okhanzhin on 11.09.2026
 */
@UtilityClass
public class TestConstants {

    public static final String IMAGE_NAME = "postgres:16-alpine";

    public static final String DATABASE_NAME = "tasks-db";
    public static final String DB_USER = "test";
    public static final String DB_PASSWORD = "test";

    public static final String LOGIN_URL = "/api/v1/auth/login";
    public static final String REGISTER_URL = "/api/v1/auth/register";
    public static final String GET_USER_URL = "/api/v1/users";
    public static final String BASE_TASK_URL = "/api/v1/tasks";

    public static final String USERNAME = "user user";
    public static final String EMAIL = "new.user@example.com";
    public static final String PASSWORD = "password12345";
    public static final String TIMEZONE = "Europe/Moscow";

    public static final String TITLE = "New task";
    public static final String DESCRIPTION = "Some description";
}
