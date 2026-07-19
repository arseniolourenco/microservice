package com.arseniolourenco.order_service.controller;

import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    @Observed()
    public String placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Order order = orderService.placeOrder(orderRequest);
        return "Order placed successfully! Order Number: " + order.getOrderNumber();
    }

    public String fallbackMethod(OrderRequest orderRequest, Exception exception) {
        System.err.println("Fallback triggered due to: " + exception.getMessage());
        exception.printStackTrace();
        return "Oops! Something went wrong, please order after some time. Reason: " + exception.getMessage();
    }
}
