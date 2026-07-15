package com.arseniolourenco.user_service.service;

import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.arseniolourenco.user_service.dto.request.UserCreateRequest;
import com.arseniolourenco.user_service.dto.request.UserUpdateRequest;
import com.arseniolourenco.user_service.dto.request.VerifyRequest;
import com.arseniolourenco.user_service.dto.response.UserResponse;
import com.arseniolourenco.user_service.exception.UserAlreadyExistsException;
import com.arseniolourenco.user_service.exception.UserNotFoundException;
import com.arseniolourenco.user_service.mapper.UserMapper;
import com.arseniolourenco.user_service.model.Role;
import com.arseniolourenco.user_service.model.User;
import com.arseniolourenco.user_service.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // CREATE
    // =========================================================

    public UserResponse createUser(UserCreateRequest request) {

        validateEmail(request.email());

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        if (user.getRole() == null) {
        user.setRole(Role.ROLE_USER);
    }

        User savedUser = userRepository.save(user);

        log.info("User created -> id={}", savedUser.getId());

        return userMapper.toUserResponse(savedUser);
    }

    // =========================================================
    // READ
    // =========================================================

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = findUserById(id);

        return userMapper.toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));

        return userMapper.toUserResponse(user);
    }

    // =========================================================
    // UPDATE
    // =========================================================

    public UserResponse updateUser(Long id, UserUpdateRequest request) {

        User existingUser = findUserById(id);

        if (!existingUser.getEmail().equals(request.email())) {
            validateEmail(request.email());
        }

        existingUser.setFirstname(request.firstname());
        existingUser.setLastname(request.lastname());
        existingUser.setEmail(request.email());

        if (request.password() != null &&
                !request.password().isBlank()) {

            existingUser.setPassword(
                    passwordEncoder.encode(request.password()));
        }

        if (request.role() != null) {
            existingUser.setRole(request.role());
        }

        User updatedUser = userRepository.save(existingUser);

        log.info("User updated -> id={}", updatedUser.getId());

        return userMapper.toUserResponse(updatedUser);
    }

    // =========================================================
    // DELETE
    // =========================================================

    public void deleteUser(Long id) {

        User user = findUserById(id);

        userRepository.delete(user);

        log.info("User deleted -> id={}", id);
    }

    // =========================================================
    // VERIFY
    // =========================================================

    public boolean verifyCredentials(VerifyRequest request) {

        return userRepository.findByEmail(request.email())
                .map(user -> passwordEncoder.matches(
                        request.password(),
                        user.getPassword()))
                .orElse(false);
    }

    // =========================================================
    // PRIVATE METHODS
    // =========================================================

    private void validateEmail(String email) {

        if (userRepository.existsByEmail(email)) {

            throw new UserAlreadyExistsException(
                    "This email is already registered");
        }
    }

    private User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));
    }
}