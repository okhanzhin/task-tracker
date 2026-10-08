package com.okhanzhin.task.tracker.backend.utils;

import lombok.experimental.UtilityClass;

/**
 *
 * @author okhanzhin on 05.10.2026
 */
@UtilityClass
public class JwtClaims {

    public static final String TYPE = "type";
    public static final String USER_ID = "userId";
    public static final String SCOPE = "scope";

    public static final String TYPE_USER = "user";
    public static final String TYPE_SERVICE = "service";

    public static final String SCOPE_REPORTS_READ = "internal:reports:read";
}
