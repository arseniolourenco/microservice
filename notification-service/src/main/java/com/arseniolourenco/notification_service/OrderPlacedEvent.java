package com.arseniolourenco.notification_service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record OrderPlacedEvent(String orderNumber) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderPlacedEvent fromJsonString(String json) {
        try {
            if (json != null && json.trim().startsWith("{")) {
                ObjectMapper mapper = new ObjectMapper();
                // Parse the inner JSON object
                return mapper.readValue(json, OrderPlacedEvent.class);
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize OrderPlacedEvent from string: {}", json, e);
        }
        // Fallback: assume the string is just the order number itself
        return new OrderPlacedEvent(json);
    }
}
