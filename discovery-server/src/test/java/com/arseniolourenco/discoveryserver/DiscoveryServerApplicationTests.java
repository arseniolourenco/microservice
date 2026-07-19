package com.arseniolourenco.discoveryserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.username=test",
        "eureka.password=test"
})
class DiscoveryServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
