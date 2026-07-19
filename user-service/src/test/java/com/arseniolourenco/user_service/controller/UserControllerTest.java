package com.arseniolourenco.user_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.arseniolourenco.user_service.dto.request.UserCreateRequest;
import com.arseniolourenco.user_service.dto.request.UserUpdateRequest;
import com.arseniolourenco.user_service.dto.response.UserResponse;
import com.arseniolourenco.user_service.model.Role;
import com.arseniolourenco.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.TestPropertySource;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Desativa o Spring Security para testes unitários do Controller
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class UserControllerTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(1L, "John", "Doe", "john.doe@example.com", Role.ROLE_USER, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
    }

    @Test
    void create_ReturnsCreatedUser() throws Exception {
        UserCreateRequest request = new UserCreateRequest("John", "Doe", "john.doe@example.com", "password123", Role.ROLE_USER, true);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void getAll_ReturnsListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    void getById_ReturnsUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void update_ReturnsUpdatedUser() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Johnny", "Doe", "johnny.doe@example.com", "newpassword", Role.ROLE_ADMIN, true);
        UserResponse updatedResponse = new UserResponse(1L, "Johnny", "Doe", "johnny.doe@example.com", Role.ROLE_ADMIN, true, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Johnny"))
                .andExpect(jsonPath("$.email").value("johnny.doe@example.com"));
    }

    @Test
    void delete_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
