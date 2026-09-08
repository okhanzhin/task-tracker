package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.dto.request.AuthenticationRequest;
import com.okhanzhin.task.tracker.backend.utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.startsWith;
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

    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String GET_USER_URL = "/api/v1/user";

    private static final String USERNAME = "user user";
    private static final String EMAIL = "new.user@example.com";
    private static final String PASSWORD = "password12345";

    @Test
    void shouldAuthenticateExistingUserAndReturnJwt() throws Exception {
        TestDataFactory.savedUser(
                USERNAME,
                EMAIL,
                PASSWORD,
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
                userRepository,
                passwordEncoder
        );

        AuthenticationRequest request = new AuthenticationRequest(EMAIL, PASSWORD);

        var registrationResponse = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String token = registrationResponse.getHeader(HttpHeaders.AUTHORIZATION);

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
}
