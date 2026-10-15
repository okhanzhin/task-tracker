package com.okhanzhin.task.tracker.scheduler.exception;

/**
 *
 * @author okhanzhin on 13.10.2026
 */
public class SummarizationException extends RuntimeException {

    public SummarizationException(String message) {
        super(message);
    }

    public SummarizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
