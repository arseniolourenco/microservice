package com.arseniolourenco.order_service.controller;

import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.dto.OrderResponse;
import com.arseniolourenco.order_service.service.OrderService;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Observed()
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @GetMapping("/{orderNumber}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        OrderResponse orderResponse = orderService.getOrder(orderNumber);
        return ResponseEntity.ok(orderResponse);
    }
}
