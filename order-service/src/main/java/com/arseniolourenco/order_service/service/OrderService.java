package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.dto.InventoryRequest;
import com.arseniolourenco.order_service.dto.InventoryResponse;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.event.OrderPlacedEvent;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.model.OrderLineItems;
import com.arseniolourenco.order_service.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.arseniolourenco.order_service.model.OutboxEvent;
import com.arseniolourenco.order_service.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    public Order placeOrder(OrderRequest orderRequest) {
        Order order = orderMapper.toOrder(orderRequest);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("PENDING");
        order.setMessage("Order received and is pending inventory validation.");

        // Save the order and outbox event
        Order savedOrder = orderRepository.save(order);
        
        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(savedOrder.getId().toString());
            outboxEvent.setAggregateType("Order");
            outboxEvent.setEventType("OrderCreated");
            
            List<OrderPlacedEvent.OrderItemDto> items = orderMapper.toOrderItemDtoList(savedOrder.getOrderLineItemsList());
                    
            outboxEvent.setPayload(objectMapper.writeValueAsString(new OrderPlacedEvent(savedOrder.getOrderNumber(), items)));
            outboxEvent.setStatus("NEW");
            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating outbox payload", e);
        }

        log.info("Order {} saved successfully with ID: {} and OutboxEvent created", savedOrder.getOrderNumber(), savedOrder.getId());
        return savedOrder;
    }


    public void updateOrderStatus(String orderNumber, String status, String message) {
        orderRepository.findByOrderNumber(orderNumber).ifPresent(order -> {
            order.setStatus(status);
            order.setMessage(message);
            orderRepository.save(order);
            log.info("Order {} status updated to: {} - {}", orderNumber, status, message);
        });
    }

}