package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.dto.request.AuthenticationRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.okhanzhin.task.tracker.backend.utils.TestConstants.BASE_TASK_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.DESCRIPTION;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.EMAIL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.GET_USER_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.LOGIN_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.PASSWORD;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TIMEZONE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TITLE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.USERNAME;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author okhanzhin on 08.09.2026
 */
public class AuthenticationTest extends IntegrationTest {

    @Test
    void shouldAuthenticateExistingUserAndReturnJwt() throws Exception {
        TestDataFactory.savedUser(
                USERNAME,
                EMAIL,
                PASSWORD,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );

        AuthenticationRequest request = new AuthenticationRequest(EMAIL, PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(header().string(
                        HttpHeaders.AUTHORIZATION,
                        startsWith("Bearer ")
                ));
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {
        TestDataFactory.savedUser(
                USERNAME,
                EMAIL,
                PASSWORD,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );

        AuthenticationRequest request = new AuthenticationRequest(EMAIL, "wrongPassword123");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorizedWhenEmailDoesNotExist() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("unknown@example.com", PASSWORD);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void shouldReturnCurrentUserUsingTokenIssuedOnAuthentication() throws Exception {
        TestDataFactory.savedUser(
                USERNAME,
                EMAIL,
                PASSWORD,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );

        AuthenticationRequest request = new AuthenticationRequest(EMAIL, PASSWORD);

        var authenticationResponse = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String token = authenticationResponse.getHeader(HttpHeaders.AUTHORIZATION);

        mockMvc.perform(get(GET_USER_URL)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing()
            throws Exception {

        mockMvc.perform(get(GET_USER_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsMalformed() throws Exception {
        mockMvc.perform(get(LOGIN_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingTaskWithoutToken() throws Exception {
        var request = new CreateTaskRequest(TITLE, DESCRIPTION, false);

        mockMvc.perform(post(BASE_TASK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        assertTrue(taskRepository.findAll().isEmpty());
    }
}
