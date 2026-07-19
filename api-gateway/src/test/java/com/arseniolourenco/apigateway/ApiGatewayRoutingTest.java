package com.arseniolourenco.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class ApiGatewayRoutingTest {

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testAuthRouteWithoutToken() {
        // As rotas de login/register são públicas
        webTestClient.post().uri("/api/auth/login")
                .exchange()
                .expectStatus().is5xxServerError(); // Porque o serviço não está a correr, o gateway pode dar 500 ou 503 Service Unavailable, 
                // mas a intenção é garantir que não dá 401 Unauthorized do Spring Security!
    }

    @Test
    void testProtectedRouteWithoutTokenIsUnauthorized() {
        webTestClient.get().uri("/api/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
