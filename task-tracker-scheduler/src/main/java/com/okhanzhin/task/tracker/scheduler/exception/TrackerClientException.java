package com.okhanzhin.task.tracker.scheduler.exception;

import lombok.Getter;

/**
 *
 * @author okhanzhin on 09.10.2026
 */
@Getter
public class TrackerClientException extends RuntimeException {

    private final Integer status;
    private final String responseBody;

    public TrackerClientException(int status, String responseBody) {
        super("Task-tracker-backend client error: status=" + status + ", body=" + responseBody);
        this.status = status;
        this.responseBody = responseBody;
    }

    public TrackerClientException(String message) {
        super(message);
        this.status = null;
        this.responseBody = null;
    }

    public TrackerClientException(String message, Throwable cause) {
        super(message, cause);
        this.status = null;
        this.responseBody = null;
    }
}
