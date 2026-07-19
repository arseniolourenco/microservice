package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.model.OutboxEvent;
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
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxRepository.findByStatus("NEW");

        for (OutboxEvent event : events) {
            try {
                // Send payload to Kafka topic "order-events"
                kafkaTemplate.send("order-events", event.getAggregateId(), event.getPayload());
                
                // Update event status to PROCESSED
                event.setStatus("PROCESSED");
                outboxRepository.save(event);
                
                log.info("Published outbox event for Order ID: {}", event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event for Order ID: {}", event.getAggregateId(), e);
                // Status remains NEW, will be retried on next poll
            }
        }
    }
}
