package com.arseniolourenco.notification_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class NotificationServiceApplication {

    private final JavaMailSender javaMailSender;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @KafkaListener(topics = "order-events", groupId = "notificationId")
    public void handleNotification(OrderPlacedEvent event, 
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   @Header(value = "eventType", required = false) String eventType) {
        if ("OrderApproved".equals(eventType)) {
            
            try {
                String orderNumber = event.orderNumber();
                if (orderNumber == null || orderNumber.isEmpty()) {
                    orderNumber = key; // fallback
                }

                log.info("Received APPROVED Notification for Order {}", orderNumber);

                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom("noreply@microservices.com");
                mailMessage.setTo("seniomauro@gmail.com");
                mailMessage.setSubject("Seu pedido foi aprovado!");
                mailMessage.setText(String.format("Olá, seu pedido %s foi aprovado pelo inventário e está sendo processado.", orderNumber));
                
                javaMailSender.send(mailMessage);
                log.info("Email enviado com sucesso para o pedido {}", orderNumber);
            } catch (Exception e) {
                log.error("Erro ao enviar email para o pedido {}", key, e);
            }
        } else {
            log.debug("Ignored event type {} for Order {}", eventType, key);
        }
    }
}

