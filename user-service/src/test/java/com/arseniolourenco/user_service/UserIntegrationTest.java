package com.arseniolourenco.user_service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.arseniolourenco.user_service.dto.request.UserCreateRequest;
import com.arseniolourenco.user_service.dto.request.UserUpdateRequest;
import com.arseniolourenco.user_service.model.Role;
import com.arseniolourenco.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("user_db_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUserAndRetrieveIt() throws Exception {
        // 1. Create User
        UserCreateRequest request = new UserCreateRequest("Integration", "Test", "integration@example.com", "pass123", Role.ROLE_USER, true);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"));

        // 2. Verify in DB
        Assertions.assertEquals(1, userRepository.count());
        Long userId = userRepository.findAll().get(0).getId();

        // 3. Retrieve User
        mockMvc.perform(get("/api/users/" + userId)
                .with(SecurityMockMvcRequestPostProcessors.jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@example.com"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        // Create user directly via API
        UserCreateRequest createReq = new UserCreateRequest("Integration", "Test", "update@example.com", "pass123", Role.ROLE_USER, true);
        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        Long userId = userRepository.findAll().get(0).getId();

        // Update User
        UserUpdateRequest updateReq = new UserUpdateRequest("Updated", "Name", "update@example.com", "newpass", Role.ROLE_USER, true);
        
        mockMvc.perform(put("/api/users/" + userId)
                .with(SecurityMockMvcRequestPostProcessors.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Updated"));

        Assertions.assertEquals("Updated", userRepository.findById(userId).get().getFirstname());
    }

    @Test
    void shouldDeleteUserAsAdmin() throws Exception {
        // Create user
        UserCreateRequest createReq = new UserCreateRequest("Integration", "Test", "delete@example.com", "pass123", Role.ROLE_USER, true);
        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        Long userId = userRepository.findAll().get(0).getId();

        // Delete User (requires ROLE_ADMIN in token)
        mockMvc.perform(delete("/api/users/" + userId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        Assertions.assertEquals(0, userRepository.count());
    }

    @Test
    void shouldFailDeleteUserAsNormalUser() throws Exception {
        // Create user
        UserCreateRequest createReq = new UserCreateRequest("Integration", "Test", "delete2@example.com", "pass123", Role.ROLE_USER, true);
        mockMvc.perform(post("/api/users")
                .with(SecurityMockMvcRequestPostProcessors.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated());

        Long userId = userRepository.findAll().get(0).getId();

        // Delete User with only ROLE_USER
        mockMvc.perform(delete("/api/users/" + userId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }
}
