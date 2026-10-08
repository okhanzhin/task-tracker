package com.okhanzhin.task.tracker.backend.exception.report;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.exception.error.ErrorMessage;
import org.springframework.http.HttpStatus;

/**
 *
 * @author okhanzhin on 07.10.2026
 */
public class InvalidReportLimitException extends ApiException {

    public InvalidReportLimitException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_REPORT_LIMIT);
    }
}
