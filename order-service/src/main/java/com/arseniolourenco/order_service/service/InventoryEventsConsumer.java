package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.event.InventoryFailedEvent;
import com.arseniolourenco.order_service.event.InventoryReservedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventsConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void consumeInventoryEvent(String message) {
        log.info("Received event on inventory-events topic: {}", message);
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            String orderNumber = rootNode.get("orderNumber").asText();

            if (rootNode.has("reason")) {
                // It's an InventoryFailedEvent
                String reason = rootNode.get("reason").asText();
                log.info("Processing InventoryFailedEvent for order: {}. Reason: {}", orderNumber, reason);
                orderService.updateOrderStatus(orderNumber, "CANCELLED", "Order Cancelled due to lack of stock: " + reason);
            } else {
                // It's an InventoryReservedEvent
                log.info("Processing InventoryReservedEvent for order: {}", orderNumber);
                orderService.updateOrderStatus(orderNumber, "CONFIRMED", "Order Confirmed and Stock Reserved successfully.");
            }
        } catch (Exception e) {
            log.error("Error processing inventory event message", e);
        }
    }
}
