package com.okhanzhin.task.tracker.backend.service.impl;

import com.okhanzhin.task.tracker.backend.exception.ApiException;
import com.okhanzhin.task.tracker.backend.exception.EmailAlreadyRegisteredException;
import com.okhanzhin.task.tracker.backend.exception.UserNotFoundException;
import com.okhanzhin.task.tracker.backend.mapper.UserMapper;
import com.okhanzhin.task.tracker.backend.model.dto.UserDTO;
import com.okhanzhin.task.tracker.backend.model.dto.request.RegisterRequest;
import com.okhanzhin.task.tracker.backend.model.dto.response.UserResponse;
import com.okhanzhin.task.tracker.backend.model.entity.UserDAO;
import com.okhanzhin.task.tracker.backend.repository.UserRepository;
import com.okhanzhin.task.tracker.backend.service.UserService;
import com.okhanzhin.task.tracker.backend.utils.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

        return userMapper.mapToFullDTO(userDAO);
    }

    @Override
    public UserResponse findByUserDetails(UserDetails userDetails) throws ApiException {
        UserDAO userDAO = userRepository.findByEmail(userDetails.getUsername()).
                orElseThrow(UserNotFoundException::new);

        return userMapper.mapToUserResponse(userDAO);
    }
}
