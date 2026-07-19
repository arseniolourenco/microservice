package com.arseniolourenco.authservice.service;

import com.arseniolourenco.authservice.config.KeycloakConfig;
import com.arseniolourenco.authservice.dto.LoginRequest;
import com.arseniolourenco.authservice.dto.RegisterRequest;
import com.arseniolourenco.authservice.dto.TokenResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final WebClient.Builder webClientBuilder;
    private final WebClient keycloakWebClient;
    private final KeycloakConfig keycloakConfig;
    private final org.keycloak.admin.client.Keycloak keycloakAdmin;

    @Override
    public TokenResponse login(LoginRequest request) {
        String tokenEndpoint = keycloakConfig.getServerUrl() + "/realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("username", request.username());
        formData.add("password", request.password());

        KeycloakTokenResponse keycloakResponse;
        try {
            keycloakResponse = keycloakWebClient
                    .post()
                    .uri(tokenEndpoint)
                    .headers(headers -> headers.setBasicAuth(keycloakConfig.getClientId(), keycloakConfig.getClientSecret()))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();
        } catch (org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (keycloakResponse == null) {
            throw new RuntimeException("Failed to get token from Keycloak");
        }

        return TokenResponse.builder()
                .accessToken(keycloakResponse.getAccessToken())
                .refreshToken(keycloakResponse.getRefreshToken())
                .expiresIn(keycloakResponse.getExpiresIn())
                .refreshExpiresIn(keycloakResponse.getRefreshExpiresIn())
                .tokenType(keycloakResponse.getTokenType())
                .build();
    }

    @Override
    public void register(RegisterRequest request) {
        // 1. Create User in Keycloak
        org.keycloak.representations.idm.UserRepresentation user = new org.keycloak.representations.idm.UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEnabled(true);

        org.keycloak.representations.idm.CredentialRepresentation credential = new org.keycloak.representations.idm.CredentialRepresentation();
        credential.setType(org.keycloak.representations.idm.CredentialRepresentation.PASSWORD);
        credential.setValue(request.password());
        credential.setTemporary(false);
        user.setCredentials(java.util.List.of(credential));

        jakarta.ws.rs.core.Response response = keycloakAdmin.realm(keycloakConfig.getRealm()).users().create(user);
        
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak, status: " + response.getStatus());
        }

        String userId = org.keycloak.admin.client.CreatedResponseUtil.getCreatedId(response);

        // Assign USER role
        try {
            org.keycloak.representations.idm.RoleRepresentation userRole = keycloakAdmin.realm(keycloakConfig.getRealm())
                    .roles().get("USER").toRepresentation();
            keycloakAdmin.realm(keycloakConfig.getRealm()).users().get(userId).roles().realmLevel().add(java.util.List.of(userRole));
        } catch (Exception e) {
            // Optional: log warning if role assignment fails
            System.err.println("Failed to assign role to user: " + e.getMessage());
        }

        // 2. Create User in user-service
        try {
            // We need a valid token to call user-service. We can use the admin token from keycloakAdmin.
            String adminToken = keycloakAdmin.tokenManager().getAccessToken().getToken();

            java.util.Map<String, Object> userCreateRequest = new java.util.HashMap<>();
            userCreateRequest.put("firstname", request.firstName());
            userCreateRequest.put("lastname", request.lastName());
            userCreateRequest.put("email", request.email());
            userCreateRequest.put("password", request.password());
            userCreateRequest.put("role", "ROLE_USER");
            userCreateRequest.put("enabled", true);

            webClientBuilder.build()
                    .post()
                    .uri("http://USER-SERVICE/api/users")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userCreateRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            // Rollback: delete from Keycloak
            keycloakAdmin.realm(keycloakConfig.getRealm()).users().get(userId).remove();
            throw new RuntimeException("Failed to create user profile in user-service, rolled back Keycloak user.", e);
        }
    }

    @Data
    static class KeycloakTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("refresh_token")
        private String refreshToken;
        
        @JsonProperty("expires_in")
        private Integer expiresIn;
        
        @JsonProperty("refresh_expires_in")
        private Integer refreshExpiresIn;
        
        @JsonProperty("token_type")
        private String tokenType;
    }
}
