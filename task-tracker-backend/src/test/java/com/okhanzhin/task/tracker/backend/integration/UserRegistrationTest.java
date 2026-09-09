package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author okhanzhin on 08.09.2026
 */
public class UserRegistrationTest extends IntegrationTest {

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String GET_USER_URL = "/api/v1/user";

    private static final String USERNAME = "user user";
    private static final String EMAIL = "new.user@example.com";
    private static final String PASSWORD = "password12345";

    @Test
    void shouldRegisterUserAndReturnJwt() throws Exception {
        var request = new RegisterRequest(
                        USERNAME,
                        EMAIL,
                        PASSWORD,
                        PASSWORD
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION));

        var savedUser = userRepository.findByEmail(EMAIL)
                .orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getPassword()).isNotEqualTo(PASSWORD);
        assertThat(passwordEncoder.matches(
                PASSWORD,
                savedUser.getPassword()
        )).isTrue();
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        var request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                PASSWORD
        );

        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturnBadRequestWhenPasswordConfirmationDoesNotMatch()
            throws Exception {

        var request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                "anotherPassword123"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid()
            throws Exception {

        var request = new RegisterRequest(
                "abc",
                "invalid-email",
                "short",
                ""
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void shouldReturnCurrentUserUsingTokenIssuedOnRegistration() throws Exception {
        var request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                PASSWORD
        );

        var registrationResponse = mockMvc.perform(post(REGISTER_URL)
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
}
