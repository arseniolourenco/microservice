package com.arseniolourenco.inventory_service.service;

import com.arseniolourenco.inventory_service.dto.InventoryRequestDTO;
import com.arseniolourenco.inventory_service.event.InventoryFailedEvent;
import com.arseniolourenco.inventory_service.event.InventoryReservedEvent;
import com.arseniolourenco.inventory_service.event.OrderPlacedEvent;
import com.arseniolourenco.inventory_service.mapper.InventoryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void consumeOrderCreatedEvent(String message) {
        log.info("Received event on order-events topic: {}", message);
        try {
            OrderPlacedEvent event = objectMapper.readValue(message, OrderPlacedEvent.class);
            log.info("Processing OrderPlacedEvent for order: {}", event.orderNumber());

            List<InventoryRequestDTO> requests = inventoryMapper.toInventoryRequestList(event.items());

            try {
                inventoryService.reduceStock(requests);
                log.info("Stock successfully reduced for order {}", event.orderNumber());

                InventoryReservedEvent reservedEvent = new InventoryReservedEvent(event.orderNumber());
                kafkaTemplate.send("inventory-events", event.orderNumber(), objectMapper.writeValueAsString(reservedEvent));
            } catch (Exception e) {
                log.error("Failed to reduce stock for order {}: {}", event.orderNumber(), e.getMessage());

                InventoryFailedEvent failedEvent = new InventoryFailedEvent(event.orderNumber(), e.getMessage());
                kafkaTemplate.send("inventory-events", event.orderNumber(), objectMapper.writeValueAsString(failedEvent));
            }

        } catch (Exception e) {
            log.error("Error processing Kafka message", e);
        }
    }
}
