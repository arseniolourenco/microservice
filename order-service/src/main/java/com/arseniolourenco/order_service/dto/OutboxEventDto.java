package com.arseniolourenco.order_service.dto;

public record OutboxEventDto(
    String aggregateId,
    String aggregateType,
    String eventType,
    String payload,
    String status
) {}
