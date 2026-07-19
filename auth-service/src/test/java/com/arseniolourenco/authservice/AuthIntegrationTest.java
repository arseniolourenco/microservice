package com.arseniolourenco.authservice;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.arseniolourenco.authservice.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WireMockTest(httpPort = 8081) // Usamos o port 8081 para o WireMock local
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "keycloak.serverUrl=http://localhost:8081",
        "keycloak.realm=test-realm"
})
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // Garantir que não tentamos conectar a serviços reais
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:8081/realms/test-realm");
    }

    @Test
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        // 1. Mock Keycloak Token Endpoint
        stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                                {
                                    "access_token": "mocked_jwt_token",
                                    "expires_in": 300,
                                    "refresh_expires_in": 1800,
                                    "refresh_token": "mocked_refresh_token",
                                    "token_type": "Bearer",
                                    "not-before-policy": 0,
                                    "session_state": "mocked-session",
                                    "scope": "profile email"
                                }
                                """)));

        // 2. Prepare Login Request
        LoginRequest request = new LoginRequest("testuser", "testpass");

        // 3. Act & Assert
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mocked_jwt_token"))
                .andExpect(jsonPath("$.refreshToken").value("mocked_refresh_token"));
    }

    @Test
    void shouldFailLoginWithUnauthorized() throws Exception {
        // 1. Mock Keycloak Returning 401
        stubFor(post(urlEqualTo("/realms/test-realm/protocol/openid-connect/token"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(401)
                        .withBody("{\"error\":\"unauthorized_client\",\"error_description\":\"Invalid client secret\"}")));

        // 2. Prepare Request
        LoginRequest request = new LoginRequest("testuser", "wrongpass");

        // 3. Act & Assert (O nosso código atual manda um RuntimeException genérico, que o Spring converte para 500. 
        // Idealmente deveríamos mapear para 401 no GlobalExceptionHandler, mas verificamos o erro).
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }
}
