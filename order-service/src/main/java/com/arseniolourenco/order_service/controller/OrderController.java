package com.arseniolourenco.order_service.controller;

import com.arseniolourenco.order_service.dto.OrderRequest;
import com.arseniolourenco.order_service.model.Order;
import com.arseniolourenco.order_service.service.OrderService;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
//    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
//    @TimeLimiter(name = "inventory")
//    @Retry(name = "inventory")
    @Observed()
    public Order placeOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return  orderService.placeOrder(orderRequest);
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please order after some time");
    }
}
