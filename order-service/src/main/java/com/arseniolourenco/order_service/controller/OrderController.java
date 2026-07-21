package com.arseniolourenco.order_service.controller;

import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.service.OrderService;

import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @Observed()
    public String placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Order order = orderService.placeOrder(orderRequest);
        return "Order placed successfully! Order Number: " + order.getOrderNumber();
    }
}
