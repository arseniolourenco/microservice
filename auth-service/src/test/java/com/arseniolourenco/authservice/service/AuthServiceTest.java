package com.arseniolourenco.authservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.arseniolourenco.authservice.config.KeycloakConfig;
import com.arseniolourenco.authservice.dto.LoginRequest;
import com.arseniolourenco.authservice.dto.RegisterRequest;
import com.arseniolourenco.authservice.dto.TokenResponse;

import jakarta.ws.rs.core.Response;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient keycloakWebClient;

    @Mock
    private KeycloakConfig keycloakConfig;

    @Mock
    private Keycloak keycloakAdmin;

    @InjectMocks
    private AuthServiceImpl authService;

    // WebClient Mocks
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        lenient().when(keycloakConfig.getServerUrl()).thenReturn("http://localhost:8080");
        lenient().when(keycloakConfig.getRealm()).thenReturn("test-realm");
        lenient().when(keycloakConfig.getClientId()).thenReturn("test-client");
        lenient().when(keycloakConfig.getClientSecret()).thenReturn("test-secret");
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("user", "password");
        
        // Emulate KeycloakTokenResponse JSON via string and map to object in Mockito, or simply mock the Mono
        // We'll create a dummy json string that gets converted
        String expectedToken = "mock_access_token";

        when(keycloakWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).body(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // We mock the return object via an inner class instance
        AuthServiceImpl.KeycloakTokenResponse mockResponse = new AuthServiceImpl.KeycloakTokenResponse();
        mockResponse.setAccessToken(expectedToken);
        mockResponse.setRefreshToken("mock_refresh");
        mockResponse.setExpiresIn(3600);
        mockResponse.setRefreshExpiresIn(7200);
        mockResponse.setTokenType("Bearer");

        when(responseSpec.bodyToMono(AuthServiceImpl.KeycloakTokenResponse.class)).thenReturn(Mono.just(mockResponse));

        TokenResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals(expectedToken, result.accessToken());
        assertEquals("mock_refresh", result.refreshToken());
    }

    @Test
    void login_Failure_ThrowsException() {
        LoginRequest request = new LoginRequest("user", "wrong_password");

        when(keycloakWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        doReturn(requestHeadersSpec).when(requestBodySpec).body(any());
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AuthServiceImpl.KeycloakTokenResponse.class)).thenReturn(Mono.error(new RuntimeException("401 Unauthorized")));

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

}
