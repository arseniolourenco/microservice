package com.arseniolourenco.order_service.event;

public record InventoryEvent(String orderNumber, String reason) {
}
