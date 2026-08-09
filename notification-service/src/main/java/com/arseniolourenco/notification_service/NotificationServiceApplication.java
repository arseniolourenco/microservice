package com.arseniolourenco.notification_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @KafkaListener(topics = "order-events", groupId = "notificationId")
    public void handleNotification(String message, 
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   @Header(value = "eventType", required = false) String eventType) {
        if ("OrderApproved".equals(eventType)) {
            // sent out an email notification
            log.info("Received APPROVED Notification for Order {}", key);
        } else {
            log.debug("Ignored event type {} for Order {}", eventType, key);
        }
    }
}

