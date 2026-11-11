package com.okhanzhin.task.tracker.scheduler.exception;

/**
 *
 * @author okhanzhin on 11.11.2026
 */
public class RetryableTaskTrackerClientException extends TaskTrackerClientException {

    public RetryableTaskTrackerClientException(int status, String responseBody) {
        super(status, responseBody);
    }

    public RetryableTaskTrackerClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
