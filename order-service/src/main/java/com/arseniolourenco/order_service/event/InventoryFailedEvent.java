package com.arseniolourenco.order_service.event;

public record InventoryFailedEvent(String orderNumber, String reason) {}
