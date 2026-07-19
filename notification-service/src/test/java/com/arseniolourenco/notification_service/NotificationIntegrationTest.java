package com.arseniolourenco.notification_service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*"
})
class NotificationIntegrationTest {

    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @SpyBean
    private NotificationServiceApplication notificationServiceApplication;

    @Test
    void shouldReceiveNotification() throws InterruptedException {
        // Arrange
        OrderPlacedEvent event = new OrderPlacedEvent("ORD-12345");

        // Act
        kafkaTemplate.send("order-events", event);

        // Wait a bit for the message to be processed
        TimeUnit.SECONDS.sleep(3);

        // Assert
        verify(notificationServiceApplication).handleNotification(any(OrderPlacedEvent.class));
    }
}
