package com.arseniolourenco.inventory_service.service;

import com.arseniolourenco.inventory_service.dto.InventoryRequestDTO;
import com.arseniolourenco.inventory_service.dto.InventoryResponseDTO;
import com.arseniolourenco.inventory_service.exception.InsufficientStockException;
import com.arseniolourenco.inventory_service.exception.SkuNotFoundException;
import com.arseniolourenco.inventory_service.mapper.InventoryMapper;
import com.arseniolourenco.inventory_service.model.InventoryModel;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

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
        InventoryModel inventory = InventoryModel.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();

        when(inventoryRepository.findBySkuCodeIn(skuCodes)).thenReturn(List.of(inventory));

        // Act
        List<InventoryResponseDTO> result = inventoryService.isInStock(skuCodes);

        // Assert
        assertEquals(1, result.size());
        assertEquals(sku, result.get(0).skuCode());
        assertTrue(result.get(0).isInStock());
        assertEquals(10, result.get(0).quantity());
    }

    @Test
    void shouldReduceStock_Success() {
        // Arrange
        String sku = "iphone_15";
        InventoryRequestDTO request = new InventoryRequestDTO(sku, 5);
        InventoryModel inventory = InventoryModel.builder()
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
        InventoryRequestDTO request = new InventoryRequestDTO(sku, 5);

        when(inventoryRepository.findBySkuCodeIn(any())).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(SkuNotFoundException.class, () -> inventoryService.reduceStock(List.of(request)));
    }

    @Test
    void shouldThrowException_WhenInsufficientStock() {
        // Arrange
        String sku = "iphone_15";
        InventoryRequestDTO request = new InventoryRequestDTO(sku, 20);
        InventoryModel inventory = InventoryModel.builder()
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
        InventoryRequestDTO request = new InventoryRequestDTO(sku, 5);
        InventoryModel inventory = InventoryModel.builder()
                .id(1L)
                .skuCode(sku)
                .quantity(10)
                .build();

        when(inventoryRepository.findBySkuCodeIn(List.of(sku))).thenReturn(List.of(inventory));

        // Act
        inventoryService.addStock(List.of(request));

        // Assert
        assertEquals(15, inventory.getQuantity());
        verify(inventoryRepository).saveAll(argThat(iterable -> {
            List<InventoryModel> list = new java.util.ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && list.get(0).getQuantity() == 15;
        }));
    }

    @Test
    void shouldAddStock_ToNewSku() {
        // Arrange
        String sku = "new_item";
        InventoryRequestDTO request = new InventoryRequestDTO(sku, 5);
        InventoryModel newInventory = new InventoryModel();
        newInventory.setSkuCode(sku);
        newInventory.setQuantity(5);

        when(inventoryRepository.findBySkuCodeIn(List.of(sku))).thenReturn(Collections.emptyList());
        when(inventoryMapper.toInventory(request)).thenReturn(newInventory);

        // Act
        inventoryService.addStock(List.of(request));

        // Assert
        verify(inventoryRepository).saveAll(argThat(iterable -> {
            List<InventoryModel> list = new java.util.ArrayList<>();
            iterable.forEach(list::add);
            return list.size() == 1 && list.get(0).getSkuCode().equals(sku) && list.get(0).getQuantity() == 5;
        }));
    }
}
