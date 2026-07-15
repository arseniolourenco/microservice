package com.arseniolourenco.order_service.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status; // NEW, SENT
    
    @Builder.Default
    private Instant createdAt = Instant.now();
}
