package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.dto.OrderResponse;
import com.arseniolourenco.order_service.event.OrderPlacedEvent;
import com.arseniolourenco.order_service.model.OrderModel;
import com.arseniolourenco.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.arseniolourenco.order_service.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.arseniolourenco.order_service.model.OutboxEvent;
import com.arseniolourenco.order_service.mapper.OrderMapper;
import org.springframework.transaction.annotation.Transactional;

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

    public OrderResponse placeOrder(OrderRequest orderRequest) {
        OrderModel order = orderMapper.toOrder(orderRequest);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("PENDING");
        order.setMessage("Order received and is pending inventory validation.");

        // Save the order and outbox event
        OrderModel savedOrder = orderRepository.save(order);
        
        try {
            List<OrderPlacedEvent.OrderItemDto> items = orderMapper.toOrderItemDtoList(savedOrder.getOrderLineItemsList());
            
            com.arseniolourenco.order_service.dto.OutboxEventDto outboxDto = new com.arseniolourenco.order_service.dto.OutboxEventDto(
                    savedOrder.getOrderNumber(),
                    "Order",
                    "OrderCreated",
                    objectMapper.writeValueAsString(new OrderPlacedEvent(savedOrder.getOrderNumber(), items)),
                    "NEW"
            );
            
            OutboxEvent outboxEvent = orderMapper.toOutboxEvent(outboxDto);
            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error creating outbox payload", e);
        }

        log.info("Order {} saved successfully with ID: {} and OutboxEvent created", savedOrder.getOrderNumber(), savedOrder.getId());
        return orderMapper.toOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderNumber) {
        OrderModel order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with order number: " + orderNumber));
        return orderMapper.toOrderResponse(order);
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