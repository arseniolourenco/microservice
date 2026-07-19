package com.arseniolourenco.order_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_outbox")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;
    private String aggregateType;
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status;
}
