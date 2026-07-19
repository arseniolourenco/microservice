package com.arseniolourenco.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    // For admin client
    private String adminUsername = "admin";
    private String adminPassword = "admin";

    @Bean
    public org.keycloak.admin.client.Keycloak keycloakAdmin() {
        return org.keycloak.admin.client.KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // usually admin operations are authenticated via master realm
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}
