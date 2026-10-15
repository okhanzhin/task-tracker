package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.okhanzhin.task.tracker.backend.utils.Constants
        .SUCCESS_USER_REGISTRATION_EMAIL_TASK_BODY;
import static com.okhanzhin.task.tracker.backend.utils.Constants
        .SUCCESS_USER_REGISTRATION_EMAIL_TASK_SUBJECT;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.EMAIL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.GET_USER_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.PASSWORD;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.REGISTER_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TIMEZONE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    @Test
    void shouldRegisterUserAndReturnJwt() throws Exception {
        RegisterRequest request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                PASSWORD,
                TIMEZONE
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(header().string(
                        HttpHeaders.AUTHORIZATION,
                        startsWith("Bearer ")
                ));

        var savedUser = userRepository.findByEmail(EMAIL)
                .orElseThrow();

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getPassword()).isNotEqualTo(PASSWORD);

        assertThat(passwordEncoder.matches(
                PASSWORD,
                savedUser.getPassword()
        )).isTrue();

        ArgumentCaptor<EmailSendingTask> captor =
                ArgumentCaptor.forClass(EmailSendingTask.class);

        verify(emailTaskProducer).send(captor.capture());

        EmailSendingTask emailTask = captor.getValue();

        assertThat(emailTask.getMessageId()).isNotNull();
        assertThat(emailTask.getRecipientEmail()).isEqualTo(EMAIL);
        assertThat(emailTask.getSubject())
                .isEqualTo(SUCCESS_USER_REGISTRATION_EMAIL_TASK_SUBJECT);
        assertThat(emailTask.getBody())
                .isEqualTo(SUCCESS_USER_REGISTRATION_EMAIL_TASK_BODY);

        verifyNoMoreInteractions(emailTaskProducer);
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists()
            throws Exception {
        RegisterRequest request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                PASSWORD,
                TIMEZONE
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

        ArgumentCaptor<EmailSendingTask> captor =
                ArgumentCaptor.forClass(EmailSendingTask.class);

        // Только первая успешная регистрация отправляет письмо.
        verify(emailTaskProducer, times(1)).send(captor.capture());

        assertThat(captor.getValue().getRecipientEmail())
                .isEqualTo(EMAIL);

        verifyNoMoreInteractions(emailTaskProducer);
    }

    @Test
    void shouldReturnBadRequestWhenPasswordConfirmationDoesNotMatch()
            throws Exception {
        RegisterRequest request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                "anotherPassword123",
                TIMEZONE
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();

        verifyNoInteractions(emailTaskProducer);
    }

    @Test
    void shouldReturnBadRequestWhenRegisterRequestIsInvalid()
            throws Exception {
        RegisterRequest request = new RegisterRequest(
                "abc",
                "invalid-email",
                "short",
                "",
                ""
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();

        verifyNoInteractions(emailTaskProducer);
    }

    @Test
    void shouldReturnCurrentUserUsingTokenIssuedOnRegistration()
            throws Exception {
        RegisterRequest request = new RegisterRequest(
                USERNAME,
                EMAIL,
                PASSWORD,
                PASSWORD,
                TIMEZONE
        );

        var response = mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(header().string(
                        HttpHeaders.AUTHORIZATION,
                        startsWith("Bearer ")
                ))
                .andReturn()
                .getResponse();

        String token = response.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        assertThat(token).isNotBlank();

        mockMvc.perform(get(GET_USER_URL)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

        verify(emailTaskProducer).send(
                org.mockito.ArgumentMatchers.any(EmailSendingTask.class)
        );

        verifyNoMoreInteractions(emailTaskProducer);
    }
}
