package com.arseniolourenco.order_service.repository;

import com.arseniolourenco.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderLineItemsList"})
    Optional<Order> findByOrderNumber(String orderNumber);
}
