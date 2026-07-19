package com.arseniolourenco.user_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        userResponse = new UserResponse(1L, "John", "Doe", "john.doe@example.com", Role.ROLE_USER, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    }

    @Test
    void createUser_Success() {
        UserCreateRequest request = new UserCreateRequest("John", "Doe", "john.doe@example.com", "password123", Role.ROLE_USER, true);
        User mappedUser = new User();
        mappedUser.setEmail(request.email());

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(mappedUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        UserCreateRequest request = new UserCreateRequest("John", "Doe", "john.doe@example.com", "password123", Role.ROLE_USER, true);

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        List<UserResponse> responses = userService.getAllUsers();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse response = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(response);
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByEmail("notfound@example.com"));
    }

    @Test
    void updateUser_Success() {
        UserUpdateRequest request = new UserUpdateRequest("Johnny", "Doe", "johnny.doe@example.com", "newpassword", Role.ROLE_ADMIN, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        UserResponse response = userService.updateUser(1L, request);

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void verifyCredentials_Success() {
        VerifyRequest request = new VerifyRequest("john.doe@example.com", "password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        boolean result = userService.verifyCredentials(request);

        assertTrue(result);
    }

    @Test
    void verifyCredentials_Failure() {
        VerifyRequest request = new VerifyRequest("john.doe@example.com", "wrongpassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        boolean result = userService.verifyCredentials(request);

        assertFalse(result);
    }
}
