package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.event.InventoryEvent;
import com.arseniolourenco.order_service.repository.OrderRepository;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.arseniolourenco.order_service.mapper.OrderMapper;
import com.arseniolourenco.order_service.dto.OutboxEventDto;
import com.arseniolourenco.order_service.model.OutboxEvent;
import com.arseniolourenco.order_service.event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventsConsumer {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    private final OrderMapper orderMapper;

    @KafkaListener(topics = "inventory-events", groupId = "order-group")
    @Transactional
    public void consumeInventoryEvent(String message) {
        log.info("Received event on inventory-events topic: {}", message);
        try {
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            log.info("Processing InventoryEvent for order: {}", event.orderNumber());

            orderRepository.findByOrderNumber(event.orderNumber()).ifPresentOrElse(order -> {
                if (event.reason() != null && !event.reason().isEmpty()) {
                    log.warn("Inventory check failed for order {}: {}. Updating status to REJECTED.", event.orderNumber(), event.reason());
                    order.setStatus("REJECTED");
                    order.setMessage(event.reason());
                } else {
                    log.info("Inventory successfully reserved for order {}. Updating status to APPROVED.", event.orderNumber());
                    order.setStatus("APPROVED");
                    order.setMessage("Inventory reserved");

                    try {
                        OutboxEventDto outboxDto = new OutboxEventDto(
                                order.getOrderNumber(),
                                "Order",
                                "OrderApproved",
                                objectMapper.writeValueAsString(new OrderPlacedEvent(order.getOrderNumber(), null)),
                                "NEW"
                        );
                        OutboxEvent outboxEvent = orderMapper.toOutboxEvent(outboxDto);
                        outboxRepository.save(outboxEvent);
                        log.info("OrderApproved event saved to outbox for order {}", order.getOrderNumber());
                    } catch (Exception ex) {
                        log.error("Failed to create OutboxEvent for OrderApproved", ex);
                    }
                }
                orderRepository.save(order);
                log.info("Order {} status updated to {}", order.getOrderNumber(), order.getStatus());
            }, () -> log.warn("Order {} not found in database", event.orderNumber()));

        } catch (Exception e) {
            log.error("Error processing Kafka message from inventory-events", e);
        }
    }
}
