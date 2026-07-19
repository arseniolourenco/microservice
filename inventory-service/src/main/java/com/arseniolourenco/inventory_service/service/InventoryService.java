package com.arseniolourenco.inventory_service.service;

import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import com.arseniolourenco.inventory_service.exception.InsufficientStockException;
import com.arseniolourenco.inventory_service.exception.SkuNotFoundException;
import com.arseniolourenco.inventory_service.model.Inventory;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    @Autowired
    private final InventoryRepository inventoryRepository;
    private final com.arseniolourenco.inventory_service.mapper.InventoryMapper inventoryMapper;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        List<Inventory> inventoryList = inventoryRepository.findBySkuCodeIn(skuCodes);

        Map<String, Integer> skuCodeToTotalQuantity = inventoryList
                .stream()
                .collect(Collectors.toMap(
                        Inventory::getSkuCode,
                        Inventory::getQuantity,
                        Integer::sum // Merge duplicates
                ));

        return skuCodes.stream().map(code -> {
            Integer totalQuantity = skuCodeToTotalQuantity.getOrDefault(code, 0);
            return new InventoryResponse(code, totalQuantity > 0, totalQuantity);
        }).collect(Collectors.toList());
    }

    @Transactional
    public void reduceStock(List<InventoryRequest> inventoryRequests) {
        // Aggregate quantities for each SKU code
        Map<String, Integer> aggregatedSkuQuantityMap = aggregateSkuQuantities(inventoryRequests);

        // Fetch inventory for the SKUs in the request
        List<Inventory> inventoryList = inventoryRepository.findBySkuCodeIn(new ArrayList<>(aggregatedSkuQuantityMap.keySet()));

        // Validate SKU existence in the inventory
        validateSkuExistence(aggregatedSkuQuantityMap.keySet(), inventoryList);

        // Map current stock levels for SKUs
        Map<String, Integer> skuStockMap = mapSkuStockLevels(inventoryList);

        // Reduce stock and handle the inventory update
        processStockReduction(aggregatedSkuQuantityMap, inventoryList, skuStockMap);
    }

    private Map<String, Integer> aggregateSkuQuantities(List<InventoryRequest> inventoryRequests) {
        Map<String, Integer> aggregatedSkuQuantityMap = new HashMap<>();
        for (InventoryRequest request : inventoryRequests) {
            aggregatedSkuQuantityMap.merge(request.skuCode(), request.quantity(), Integer::sum);
        }
        return aggregatedSkuQuantityMap;
    }

    private void validateSkuExistence(Collection<String> skuCodes, List<Inventory> inventoryList) {
        // Create a set of existing SKU codes from the inventory
        Set<String> existingSkuCodes = inventoryList.stream()
                .map(Inventory::getSkuCode)
                .collect(Collectors.toSet());

        // Identify missing SKU codes by checking which requested SKU codes are not in the existing set
        List<String> missingSkuCodes = skuCodes.stream()
                .filter(skuCode -> !existingSkuCodes.contains(skuCode))
                .collect(Collectors.toList());

        // If there are any missing SKUs, log a warning and throw an exception
        if (!missingSkuCodes.isEmpty()) {
            log.warn("Missing SKU Codes: {}", missingSkuCodes);
            throw new SkuNotFoundException("SKU Code(s) not found in inventory: " + String.join(", ", missingSkuCodes));
        }
    }

    private Map<String, Integer> mapSkuStockLevels(List<Inventory> inventoryList) {
        Map<String, Integer> skuStockMap = new HashMap<>();
        for (Inventory inventory : inventoryList) {
            skuStockMap.merge(inventory.getSkuCode(), inventory.getQuantity(), Integer::sum);
        }
        return skuStockMap;
    }


    private void processStockReduction(Map<String, Integer> aggregatedSkuQuantityMap,
                                       List<Inventory> inventoryList,
                                       Map<String, Integer> skuStockMap) {
        List<Inventory> updatedInventories = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : aggregatedSkuQuantityMap.entrySet()) {
            String skuCode = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            Integer currentStock = skuStockMap.getOrDefault(skuCode, 0);

            if (currentStock < requestedQuantity) {
                throw new InsufficientStockException("Insufficient stock for SKU: " + skuCode
                        + ". Available: " + currentStock
                        + ", Required: " + requestedQuantity);
            }

            int quantityToReduce = requestedQuantity;
            for (Inventory inventory : inventoryList) {
                if (inventory.getSkuCode().equals(skuCode) && quantityToReduce > 0) {
                    int reduceAmount = Math.min(inventory.getQuantity(), quantityToReduce);
                    inventory.setQuantity(inventory.getQuantity() - reduceAmount);
                    quantityToReduce -= reduceAmount;
                    updatedInventories.add(inventory);
                }
                if (quantityToReduce <= 0) break;
            }
        }

        inventoryRepository.saveAll(updatedInventories); // 🚀 batch save
    }


    @Transactional
    public void addStock(List<InventoryRequest> inventoryRequests) {
        List<String> skuCodes = inventoryRequests.stream()
                .map(InventoryRequest::skuCode)
                .toList();

        List<Inventory> existingInventories = inventoryRepository.findBySkuCodeIn(skuCodes);
        Map<String, Inventory> existingInventoryMap = existingInventories.stream()
                .collect(Collectors.toMap(Inventory::getSkuCode, inv -> inv));

        List<Inventory> inventoriesToSave = new ArrayList<>();

        for (InventoryRequest request : inventoryRequests) {
            Inventory inventory = existingInventoryMap.get(request.skuCode());
            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + request.quantity());
                inventoriesToSave.add(inventory);
            } else {
                inventoriesToSave.add(inventoryMapper.toInventory(request));
            }
        }

        inventoryRepository.saveAll(inventoriesToSave);
    }
}