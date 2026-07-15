package com.arseniolourenco.order_service.repository;

import com.arseniolourenco.order_service.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

     List<OutboxEvent> findByStatus(String status);
}
