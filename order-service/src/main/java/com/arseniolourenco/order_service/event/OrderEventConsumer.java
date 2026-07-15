package com.arseniolourenco.order_service.event;

import com.arseniolourenco.order_service.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    public void consumeInventoryEvent(String payload) {
        log.info("Received event from inventory-events: {}", payload);
        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventType = root.get("eventType").asText();
            String orderNumber = root.get("orderNumber").asText();

            if ("StockReserved".equals(eventType)) {
                orderService.updateOrderStatus(orderNumber, "CONFIRMED", "Stock reserved successfully");
            } else if ("StockRejected".equals(eventType)) {
                String error = root.has("error") ? root.get("error").asText() : "Insufficient stock";
                orderService.updateOrderStatus(orderNumber, "CANCELLED", error);
            }
        } catch (Exception e) {
            log.error("Failed to process inventory event", e);
        }
    }
}
