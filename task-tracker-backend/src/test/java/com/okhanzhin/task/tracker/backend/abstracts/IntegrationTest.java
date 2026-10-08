package com.okhanzhin.task.tracker.backend.abstracts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okhanzhin.task.tracker.backend.repository.TaskRepository;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.backend.service.JwtService;
import com.okhanzhin.task.tracker.backend.producer.EmailTaskProducer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.okhanzhin.task.tracker.backend.utils.TestConstants.DATABASE_NAME;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.DB_PASSWORD;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.DB_USER;
import static com.okhanzhin.task.tracker.backend.utils.TestConstants.IMAGE_NAME;

/**
 *
 * @author okhanzhin on 08.09.2026
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(IMAGE_NAME)
                    .withDatabaseName(DATABASE_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @MockitoBean
    protected EmailTaskProducer emailTaskProducer;

    @BeforeEach
    void clearDatabaseBeforeEachTest() {
        clearDatabase();
    }

    protected void clearDatabase() {
        userRepository.deleteAll();
    }
}
