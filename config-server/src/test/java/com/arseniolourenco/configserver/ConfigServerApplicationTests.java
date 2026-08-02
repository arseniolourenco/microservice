package com.arseniolourenco.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.profiles.active=native")
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
