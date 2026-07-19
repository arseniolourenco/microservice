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

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient.Builder restClientBuilder;
    private final Tracer tracer;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    public Order placeOrder(OrderRequest orderRequest) {
        Order order = orderMapper.toOrder(orderRequest);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus("PENDING");



        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        List<Integer> quantities = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getQuantity)
                .toList();

        // Check if the items are in stock
        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {
            inventoryServiceLookup.event("Inventory stock verified");
            
            checkInventoryStock(skuCodes);
            reduceInventoryStock(skuCodes, quantities);

            // Save the order and outbox event
            Order savedOrder = orderRepository.save(order);
            
            try {
                OutboxEvent outboxEvent = new OutboxEvent();
                outboxEvent.setAggregateId(savedOrder.getId().toString());
                outboxEvent.setAggregateType("Order");
                outboxEvent.setEventType("OrderCreated");
                outboxEvent.setPayload(objectMapper.writeValueAsString(new OrderPlacedEvent(savedOrder.getOrderNumber())));
                outboxEvent.setStatus("NEW");
                outboxRepository.save(outboxEvent);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error creating outbox payload", e);
            }

            log.info("Order {} saved successfully with ID: {}", savedOrder.getOrderNumber(), savedOrder.getId());
            return savedOrder;
        } finally {
            inventoryServiceLookup.end();
        }
    }

    private Span startInventoryServiceSpan() {
        return tracer.nextSpan().name("InventoryServiceLookup").start();
    }

    private void checkInventoryStock(List<String> skuCodes) {
        InventoryResponse[] inventoryResponseArray = restClientBuilder.build().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventory")
                        .queryParam("skuCode", skuCodes.toArray())
                        .build())
                .retrieve()
                .body(InventoryResponse[].class);

        if (inventoryResponseArray == null) {
            throw new IllegalArgumentException("Failed to check inventory status");
        }

        log.info("Received inventoryResponses: {}", Arrays.toString(inventoryResponseArray));
        boolean allInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

        log.info("Inventory check for SKUs {}: {}", skuCodes, allInStock ? "All in stock" : "Some out of stock");
        
        if (!allInStock) {
            throw new IllegalArgumentException("One or more products are not in stock, please try again later.");
        }
    }

//    private void reduceInventoryStock(List<String> skuCodes, List<Integer> quantities) {
//    String response = webClientBuilder.build()
//            .post()
//            .uri(uriBuilder -> uriBuilder
//                    .path("/api/inventory/reduce")
//                    .queryParam("skuCode", skuCodes.toArray())
//                    .queryParam("quantity", quantities.toArray())
//                    .build())
//            .retrieve()
//            .bodyToMono(String.class)
//            .onErrorResume(e -> {
//                log.error("Error reducing inventory: {}", e.getMessage());
//                throw new RuntimeException("Inventory service error: " + e.getMessage());
//            })
//            .block();
//
//        log.info("Inventory reduction response: {}", response);
//    }

    private void reduceInventoryStock(List<String> skuCodes, List<Integer> quantities) {
        // Create a list of InventoryRequest objects from the provided SKU codes and quantities
        List<InventoryRequest> inventoryRequests = new ArrayList<>();
        for (int i = 0; i < skuCodes.size(); i++) {
            inventoryRequests.add(new InventoryRequest(skuCodes.get(i), quantities.get(i)));
        }

        try {
            String response = restClientBuilder.build()
                    .post()
                    .uri("/api/inventory/reduce")
                    .body(inventoryRequests) // Pass the list of InventoryRequest objects as the JSON request body
                    .retrieve()
                    .body(String.class);

            log.info("Inventory reduction response: {}", response);
        } catch (Exception e) {
            log.error("Error reducing inventory: {}", e.getMessage());
            throw new RuntimeException("Inventory service error: " + e.getMessage());
        }
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