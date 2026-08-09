package com.arseniolourenco.notification_service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
        "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.properties.spring.json.trusted.packages=*",
        "spring.kafka.consumer.auto-offset-reset=earliest"
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
        
        Message<OrderPlacedEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, "order-events")
                .setHeader(KafkaHeaders.KEY, "ORD-12345")
                .setHeader("eventType", "OrderApproved")
                .build();

        // Act
        kafkaTemplate.send(message);

        // Wait a bit for the message to be processed
        TimeUnit.SECONDS.sleep(3);

        // Assert
        verify(notificationServiceApplication).handleNotification(any(String.class), eq("ORD-12345"), eq("OrderApproved"));
    }
}
