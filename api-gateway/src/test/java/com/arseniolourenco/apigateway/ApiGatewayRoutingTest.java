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
                // Em contexto de teste sem o config-server (sem rotas carregadas),
                // uma rota pública simplesmente retorna 404 Not Found e não 401 Unauthorized
                .expectStatus().isNotFound();
    }

    @Test
    void testProtectedRouteWithoutTokenIsUnauthorized() {
        // Rotas como /api/orders ou /api/products são protegidas por defeito
        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
