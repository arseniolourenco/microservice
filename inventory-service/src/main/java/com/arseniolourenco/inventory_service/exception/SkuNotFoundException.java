package com.arseniolourenco.inventory_service.exception;

public class SkuNotFoundException extends RuntimeException {
    public SkuNotFoundException(String message) {
        super(message);
    }
}
