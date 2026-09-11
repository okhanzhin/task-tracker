package com.okhanzhin.task.tracker.backend.integration;

import com.okhanzhin.task.tracker.backend.abstracts.IntegrationTest;
import com.okhanzhin.task.tracker.backend.model.dto.request.AuthenticationRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.CreateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.request.UpdateTaskRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.TaskResponse;
import com.okhanzhin.task.tracker.backend.model.entity.TaskDAO;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.utils.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.okhanzhin.task.tracker.backend.utils.TestConstants.BASE_TASK_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.DESCRIPTION;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.EMAIL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.LOGIN_URL;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.PASSWORD;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TIMEZONE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.TITLE;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.USERNAME;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 * @author okhanzhin on 11.09.2026
 */
public class TaskManagementTest extends IntegrationTest {

    @Test
    void shouldCreateTaskAndReturn() throws Exception {
        var token = createUserAndGetToken();
        var createTaskRequest = new CreateTaskRequest(TITLE, DESCRIPTION, false);

        var response = mockMvc.perform(post(BASE_TASK_URL)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, startsWith(BASE_TASK_URL + "/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value(TITLE))
                .andExpect(jsonPath("$.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.completed").value(false))
                .andReturn().getResponse();

        var taskResponse = objectMapper.readValue(
                response.getContentAsString(),
                TaskResponse.class
        );
        var savedTask = taskRepository.findById(taskResponse.getId()).orElseThrow();

        assertEquals(TITLE, savedTask.getTitle());
        assertEquals(DESCRIPTION, savedTask.getDescription());
        assertFalse(savedTask.isCompleted());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithBlankTitle() throws Exception {
        var token = createUserAndGetToken();

        var request = new CreateTaskRequest(
                "   ",
                DESCRIPTION,
                false
        );

        mockMvc.perform(post(BASE_TASK_URL)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertTrue(taskRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingTaskWithTooLongDescription() throws Exception {
        var token = createUserAndGetToken();

        var request = new CreateTaskRequest(
                TITLE,
                "a".repeat(5001),
                false
        );

        mockMvc.perform(post(BASE_TASK_URL)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertTrue(taskRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnAllTasksOfCurrentUser() throws Exception {
        var user = createUser(USERNAME, EMAIL, PASSWORD);
        createTask(TITLE, DESCRIPTION, user);

        String token = getToken(EMAIL, PASSWORD);

        mockMvc.perform(get(BASE_TASK_URL)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").value(TITLE))
                .andExpect(jsonPath("$[0].description").value(DESCRIPTION))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    void shouldReturnOnlyTasksOfCurrentUser() throws Exception {
        var currentUser = createUser(USERNAME, EMAIL, PASSWORD);

        TestDataFactory.savedTask(
                "Current user task",
                DESCRIPTION,
                false,
                currentUser,
                taskRepository
        );

        var anotherUser = createUser(
                "another-user",
                "another@example.com",
                "another-password"
        );

        TestDataFactory.savedTask(
                "Another user task",
                DESCRIPTION,
                false,
                anotherUser,
                taskRepository
        );

        var token = getToken(EMAIL, PASSWORD);

        mockMvc.perform(get(BASE_TASK_URL)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Current user task"));
    }

    @Test
    void shouldReturnTaskByIdForOwner() throws Exception {
        var user = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, user);

        var token = getToken(EMAIL, PASSWORD);

        mockMvc.perform(get(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(TITLE))
                .andExpect(jsonPath("$.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenGettingTaskOfAnotherUser() throws Exception {
        var owner = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, owner);

        var anotherEmail = "another@example.com";
        var anotherPassword = "another-password";

        createUser("another-user",
                anotherEmail,
                anotherPassword
        );

        var anotherUserToken = getToken(anotherEmail, anotherPassword);

        mockMvc.perform(get(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, anotherUserToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentTask() throws Exception {
        var token = createUserAndGetToken();

        mockMvc.perform(get(BASE_TASK_URL + "/{taskId}", 999_999L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateTaskAndReturnUpdatedResponse() throws Exception {
        var user = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, user);
        var token = getToken(EMAIL, PASSWORD);

        var updatedTitle = "Updated task title";
        var updatedDescription = "Updated task description";

        var request = new UpdateTaskRequest(
                updatedTitle,
                updatedDescription,
                true
        );

        mockMvc.perform(patch(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(updatedTitle))
                .andExpect(jsonPath("$.description").value(updatedDescription))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());

        TaskDAO updatedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertEquals(updatedTitle, updatedTask.getTitle());
        assertEquals(updatedDescription, updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
        assertNotNull(updatedTask.getCompletedAt());
    }

    @Test
    void shouldReturnBadRequestAndNotUpdateTaskWhenTitleIsBlank() throws Exception {
        var user = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, user);
        var token = getToken(EMAIL, PASSWORD);

        var request = new UpdateTaskRequest(
                "   ",
                "New description",
                true
        );

        mockMvc.perform(patch(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        var unchangedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertEquals(TITLE, unchangedTask.getTitle());
        assertEquals(DESCRIPTION, unchangedTask.getDescription());
        assertFalse(unchangedTask.isCompleted());
        assertNull(unchangedTask.getCompletedAt());
    }

    @Test
    void shouldReturnNotFoundAndNotUpdateTaskOfAnotherUser() throws Exception {
        var owner = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, owner);

        String anotherEmail = "another@example.com";
        String anotherPassword = "another-password";

        createUser("another-user", anotherEmail, anotherPassword);
        var anotherUserToken = getToken(anotherEmail, anotherPassword);

        var request = new UpdateTaskRequest(
                "Hacked title",
                "Hacked description",
                true
        );

        mockMvc.perform(patch(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, anotherUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        var unchangedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertEquals(TITLE, unchangedTask.getTitle());
        assertEquals(DESCRIPTION, unchangedTask.getDescription());
        assertFalse(unchangedTask.isCompleted());
        assertNull(unchangedTask.getCompletedAt());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        var user = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, user);
        var token = getToken(EMAIL, PASSWORD);

        mockMvc.perform(delete(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.existsById(task.getId()));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentTask() throws Exception {
        var token = createUserAndGetToken();

        mockMvc.perform(delete(BASE_TASK_URL + "/{taskId}", 999_999L)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnNotFoundAndNotDeleteTaskOfAnotherUser() throws Exception {
        var owner = createUser(USERNAME, EMAIL, PASSWORD);
        var task = createTask(TITLE, DESCRIPTION, owner);

        String anotherUsername = "another-user";
        String anotherEmail = "another@example.com";
        String anotherPassword = "another-password";

        createUser(anotherUsername, anotherEmail, anotherPassword);

        var anotherUserToken = getToken(anotherEmail, anotherPassword);

        mockMvc.perform(delete(BASE_TASK_URL + "/{taskId}", task.getId())
                        .header(HttpHeaders.AUTHORIZATION, anotherUserToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        assertTrue(taskRepository.existsById(task.getId()));
    }

    private UserDAO createUser(String username, String email, String password) {
        return TestDataFactory.savedUser(
                username,
                email,
                password,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );
    }

    private TaskDAO createTask(String title, String description, UserDAO user) {
        return TestDataFactory.savedTask(
                title,
                description,
                false,
                user,
                taskRepository
        );
    }

    private String createUserAndGetToken() throws Exception {
        TestDataFactory.savedUser(
                USERNAME,
                EMAIL,
                PASSWORD,
                TIMEZONE,
                userRepository,
                passwordEncoder
        );

        return getToken(EMAIL, PASSWORD);
    }

    private String getToken(String email, String password) throws Exception {
        var authenticationRequest = new AuthenticationRequest(email, password);

        var authenticationResponse = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        return authenticationResponse.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
