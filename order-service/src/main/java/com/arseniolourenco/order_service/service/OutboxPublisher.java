package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.event.OutboxEvent;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Método agendado que roda a cada 5 segundos para publicar eventos Kafka
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        // 1️⃣ Buscar eventos novos
        List<OutboxEvent> events = outboxRepository.findByStatus("NEW");

        if (events.isEmpty()) {
            return;
        }

        log.info("Encontrados {} eventos para publicar no Kafka", events.size());

        // 2️⃣ Processar cada evento
        for (OutboxEvent event : events) {
            try {
                // Publicar no Kafka
                kafkaTemplate.send("order-events", event.getPayload());

                // Marcar como enviado
                event.setStatus("SENT");
                outboxRepository.save(event);

                log.info("Evento {} publicado no Kafka com sucesso", event.getId());
            } catch (Exception e) {
                log.error("Falha ao publicar evento {}: {}", event.getId(), e.getMessage());
                // Não lança exceção, continua com os próximos eventos
            }
        }
    }
}