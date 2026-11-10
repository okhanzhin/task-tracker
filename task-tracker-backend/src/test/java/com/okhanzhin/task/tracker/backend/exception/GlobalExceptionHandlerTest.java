package com.okhanzhin.task.tracker.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author okhanzhin on 10.11.2026
 */
public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldReturn400ForUnreadableRequestBody() {
        var exception = new HttpMessageNotReadableException(
                "Malformed JSON",
                new MockHttpInputMessage(new byte[0])
        );

        var response = handler.handleMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldPreserve405AndAllowHeader() {
        var exception = new HttpRequestMethodNotSupportedException(
                "PUT",
                List.of("GET", "POST")
        );

        var response = handler.handleGenericException(exception);

        assertEquals(
                HttpStatus.METHOD_NOT_ALLOWED,
                response.getStatusCode()
        );

        assertEquals(
                java.util.Set.of(HttpMethod.GET, HttpMethod.POST),
                response.getHeaders().getAllow()
        );
    }

    @Test
    void shouldPreserveSpringErrorStatus() {
        var exception = new ResponseStatusException(
                HttpStatus.NOT_FOUND
        );

        var response = handler.handleGenericException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturn500ForUnexpectedException() {
        var response = handler.handleGenericException(
                new IllegalStateException("Unexpected failure")
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
    }
}
