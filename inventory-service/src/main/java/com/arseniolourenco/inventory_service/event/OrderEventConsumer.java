package com.arseniolourenco.inventory_service.event;

import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.exception.InsufficientStockException;
import com.arseniolourenco.inventory_service.exception.SkuNotFoundException;
import com.arseniolourenco.inventory_service.service.InventoryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    @org.springframework.transaction.annotation.Transactional
    public void consumeOrderEvent(String payload) {
        log.info("Received event from order-events: {}", payload);
        try {
            JsonNode root = objectMapper.readTree(payload);
            String orderNumber = root.get("orderNumber").asText();

            List<InventoryRequest> requests = new ArrayList<>();
            JsonNode items = root.get("orderLineItemsList");
            if (items != null && items.isArray()) {
                log.info("Found {} line items to process for order {}", items.size(), orderNumber);
                for (JsonNode item : items) {
                    String sku = item.get("skuCode").asText();
                    int qty = item.get("quantity").asInt();
                    log.info("Adding to reduction request: SKU={}, Qty={}", sku, qty);
                    requests.add(new InventoryRequest(sku, qty));
                }
            } else {
                log.error("Order {} has no items in the payload! JSON: {}", orderNumber, payload);
            }

            try {
                // Tenta reservar o stock
                inventoryService.reduceStock(requests);
                
                // Publica sucesso
                log.info("Stock reserved for Order {}", orderNumber);
                publishInventoryEvent("StockReserved", orderNumber, null);

            } catch (InsufficientStockException | SkuNotFoundException ex) {
                // Publica falha de negócio
                log.warn("Stock reservation failed for Order {}: {}", orderNumber, ex.getMessage());
                publishInventoryEvent("StockRejected", orderNumber, ex.getMessage());
            }

        } catch (Exception e) {
            log.error("Failed to process order event", e);
        }
    }

    private void publishInventoryEvent(String eventType, String orderNumber, String errorMsg) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode response = objectMapper.createObjectNode();
            response.put("eventType", eventType);
            response.put("orderNumber", orderNumber);
            response.put("error", errorMsg != null ? errorMsg : "");

            kafkaTemplate.send("inventory-events", response.toString());
        } catch (Exception e) {
            log.error("Failed to publish inventory event", e);
        }
    }
}
