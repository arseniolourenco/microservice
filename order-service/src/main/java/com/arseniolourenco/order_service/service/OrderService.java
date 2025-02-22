package com.arseniolourenco.order_service.service;

import com.arseniolourenco.order_service.dto.InventoryRequest;
import com.arseniolourenco.order_service.dto.InventoryResponse;
import com.arseniolourenco.order_service.dto.OrderLineItemsDto;
import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.event.OrderPlacedEvent;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.model.OrderLineItems;
import com.arseniolourenco.order_service.repository.OrderRepository;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public Order placeOrder(OrderRequest orderRequest) {
        Order order = initializeOrder(orderRequest);
        Map<String, Integer> skuQuantityMap = aggregateSkuCodesAndQuantities(order);

        List<String> skuCodes = new ArrayList<>(skuQuantityMap.keySet());
        List<String> aggregatedSkuCodes = new ArrayList<>(skuQuantityMap.keySet());
        List<Integer> aggregatedQuantities = new ArrayList<>(skuQuantityMap.values());

        // Check if the items are in stock
        if (checkInventoryStock(skuCodes)) {
            // Reduce inventory stock if items are in stock
            try (Tracer.SpanInScope spanInScope = tracer.withSpan(startInventoryServiceSpan())) {
                log.info("Checking inventory for SKUs: {}", aggregatedSkuCodes);
                checkInventoryStock(aggregatedSkuCodes);
                reduceInventoryStock(aggregatedSkuCodes, aggregatedQuantities);

                // Save the order and publish the event
                Order savedOrder = orderRepository.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(savedOrder.getOrderNumber()));
                log.info("Order {} saved successfully with ID: {}", savedOrder.getOrderNumber(), savedOrder.getId());
                return savedOrder;
            } finally {
                tracer.currentSpan().end();
            }
        } else {
            log.error("Product is not in stock. Please try again later.");
            throw new RuntimeException("Product is not in stock. Please try again later.");
        }
    }

    private Span startInventoryServiceSpan() {
        return tracer.nextSpan().name("InventoryServiceLookup").start();
    }

    private Order initializeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemsList(orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToEntity)
                .toList());
        return order;
    }

    private OrderLineItems mapToEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }

    private Map<String, Integer> aggregateSkuCodesAndQuantities(Order order) {
        List<OrderLineItems> orderLineItems = order.getOrderLineItemsList();
        List<String> skuCodes = orderLineItems.stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
        List<Integer> quantities = orderLineItems.stream()
                .map(OrderLineItems::getQuantity)
                .toList();

        return IntStream.range(0, skuCodes.size())
                .boxed()
                .collect(Collectors.toMap(
                        skuCodes::get,
                        quantities::get,
                        Integer::sum // Aggregate quantities for duplicate SKUs
                ));
    }

    private boolean checkInventoryStock(List<String> skuCodes) {
        InventoryResponse[] inventoryResponses = webClientBuilder.build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http") // Specify the scheme explicitly
                        .host("inventory-service") // Specify the host explicitly
                        .path("/api/inventory") // Add the path to the inventory service endpoint
                        .queryParam("skuCode", skuCodes.toArray()) // Correctly add query parameters
                        .build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        assert inventoryResponses != null;
        boolean allInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);

        log.info("Inventory check for SKUs {}: {}", skuCodes, allInStock ? "All in stock" : "Some out of stock");
        return allInStock;
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

        String response = webClientBuilder.build()
                .post()
                .uri("/api/inventory/reduce")
                .bodyValue(inventoryRequests) // Pass the list of InventoryRequest objects as the JSON request body
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    log.error("Error reducing inventory: {}", e.getMessage());
                    throw new RuntimeException("Inventory service error: " + e.getMessage());
                })
                .block();

        log.info("Inventory reduction response: {}", response);
    }

}