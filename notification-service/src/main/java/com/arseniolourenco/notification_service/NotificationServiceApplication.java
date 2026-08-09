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
    public void handleNotification(String message, 
                                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
                                   @Header(value = "eventType", required = false) String eventType) {
        if ("OrderApproved".equals(eventType)) {
            log.info("Received APPROVED Notification for Order {}", key);
            
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom("noreply@microservices.com");
                mailMessage.setTo("seniomauro@gmail.com");
                mailMessage.setSubject("Seu pedido foi aprovado!");
                mailMessage.setText(String.format("Olá, seu pedido %s foi aprovado pelo inventário e está sendo processado.", key));
                
                javaMailSender.send(mailMessage);
                log.info("Email enviado com sucesso para o pedido {}", key);
            } catch (Exception e) {
                log.error("Erro ao enviar email para o pedido {}", key, e);
            }
        } else {
            log.debug("Ignored event type {} for Order {}", eventType, key);
        }
    }
}

