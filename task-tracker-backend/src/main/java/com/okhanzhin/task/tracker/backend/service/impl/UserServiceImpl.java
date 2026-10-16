package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.exception.EmailAlreadyRegisteredException;
import com.okhanzhin.task.tracker.backend.exception.UserNotFoundException;
import com.okhanzhin.task.tracker.backend.mapper.UserMapper;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.producer.EmailTaskProducer;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.backend.service.UserService;
import com.okhanzhin.task.tracker.backend.utils.Constants;
import com.okhanzhin.task.tracker.common.model.messaging.mail.EmailSendingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.okhanzhin.task.tracker.backend.utils.Constants.SUCCESS_USER_REGISTRATION_EMAIL_TASK_BODY;
import static com.okhanzhin.task.tracker.backend.utils.Constants.SUCCESS_USER_REGISTRATION_EMAIL_TASK_SUBJECT;
import static com.okhanzhin.task.tracker.backend.utils.ConstrainViolationUtils.hasConstraint;

/**
 *
 * @author okhanzhin on 25.08.2026
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailTaskProducer emailTaskProducer;

    @Override
    public UserDTO createUser(RegisterRequest request) {
        var userDAO = userMapper.map(request);
        try {
            userRepository.save(userDAO);
            log.info("User {} was successfully saved to the database.", userDAO.getUsername());

        } catch (DataIntegrityViolationException e) {
            if (hasConstraint(e, Constants.UK_USERS_EMAIL)) {
                throw new EmailAlreadyRegisteredException();
            }
        }
        emailTaskProducer.send(EmailSendingTask.builder()
                .messageId(UUID.randomUUID())
                .recipientEmail(userDAO.getEmail())
                .subject(SUCCESS_USER_REGISTRATION_EMAIL_TASK_SUBJECT)
                .body(SUCCESS_USER_REGISTRATION_EMAIL_TASK_BODY).build());

        return userMapper.mapToFullDTO(userDAO);
    }

    @Override
    public UserResponse findByUserDetails(UserDetails userDetails) throws ApiException {
        UserDAO userDAO = userRepository.findByEmail(userDetails.getUsername()).
                orElseThrow(UserNotFoundException::new);

        return userMapper.mapToUserResponse(userDAO);
    }
}
