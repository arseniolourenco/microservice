package com.arseniolourenco.order_service.repository;

import com.arseniolourenco.order_service.model.OrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orderLineItemsList"})
    Optional<OrderModel> findByOrderNumber(String orderNumber);
}
