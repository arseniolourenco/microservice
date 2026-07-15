package com.arseniolourenco.inventory_service.service;

import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import com.arseniolourenco.inventory_service.exception.InsufficientStockException;
import com.arseniolourenco.inventory_service.exception.SkuNotFoundException;
import com.arseniolourenco.inventory_service.mapper.InventoryMapper;
import com.arseniolourenco.inventory_service.model.Inventory;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void shouldCheckStock_WhenSkuExists() {
        // Arrange
        String sku = "iphone_15";
        List<String> skuCodes = List.of(sku);
        Inventory inventory = Inventory.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();
        InventoryResponse mockResponse = new InventoryResponse(sku, true, 10);

        when(inventoryRepository.findBySkuCodeIn(skuCodes)).thenReturn(List.of(inventory));

        // Act
        List<InventoryResponse> responses = inventoryService.isInStock(skuCodes);

        // Assert
        assertFalse(responses.isEmpty());
        assertEquals(sku, responses.get(0).getSkuCode());
        assertTrue(responses.get(0).isInStock());
        assertEquals(10, responses.get(0).getQuantity());
    }

    @Test
    void shouldReduceStock_Success() {
        // Arrange
        String sku = "iphone_15";
        InventoryRequest request = new InventoryRequest(sku, 5);
        Inventory inventory = Inventory.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();

        when(inventoryRepository.findBySkuCodeIn(any())).thenReturn(List.of(inventory));

        // Act
        inventoryService.reduceStock(List.of(request));

        // Assert
        assertEquals(5, inventory.getQuantity());
        verify(inventoryRepository).saveAll(any());
    }

    @Test
    void shouldThrowException_WhenSkuNotFound() {
        // Arrange
        String sku = "unknown";
        InventoryRequest request = new InventoryRequest(sku, 5);

        when(inventoryRepository.findBySkuCodeIn(any())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(SkuNotFoundException.class, () -> inventoryService.reduceStock(List.of(request)));
    }

    @Test
    void shouldThrowException_WhenInsufficientStock() {
        // Arrange
        String sku = "iphone_15";
        InventoryRequest request = new InventoryRequest(sku, 20);
        Inventory inventory = Inventory.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();

        when(inventoryRepository.findBySkuCodeIn(any())).thenReturn(List.of(inventory));

        // Act & Assert
        assertThrows(InsufficientStockException.class, () -> inventoryService.reduceStock(List.of(request)));
    }

    @Test
    void shouldAddStock_ToExistingSku() {
        // Arrange
        String sku = "iphone_15";
        InventoryRequest request = new InventoryRequest(sku, 5);
        Inventory inventory = Inventory.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();

        when(inventoryRepository.findBySkuCodeIn(List.of(sku))).thenReturn(List.of(inventory));

        // Act
        inventoryService.addStock(List.of(request));

        // Assert
        assertEquals(15, inventory.getQuantity());
        verify(inventoryRepository).save(inventory);
        verify(inventoryRepository).flush();
    }

    @Test
    void shouldAddStock_ToNewSku() {
        // Arrange
        String sku = "new_item";
        InventoryRequest request = new InventoryRequest(sku, 5);

        when(inventoryRepository.findBySkuCodeIn(List.of(sku))).thenReturn(Collections.emptyList());

        // Act
        inventoryService.addStock(List.of(request));

        // Assert
        verify(inventoryRepository).save(argThat(inv -> 
            inv != null && inv.getSkuCode().equals(sku) && inv.getQuantity() == 5
        ));
        verify(inventoryRepository).flush();
    }
}
