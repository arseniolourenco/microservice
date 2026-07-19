package com.arseniolourenco.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.arseniolourenco.order_service.model.OutboxEvent;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

     List<OutboxEvent> findByStatus(String status);
}
