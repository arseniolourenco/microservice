package com.arseniolourenco.inventory_service.event;

public record InventoryFailedEvent(String orderNumber, String reason) {}
