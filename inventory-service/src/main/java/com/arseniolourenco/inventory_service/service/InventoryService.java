package com.arseniolourenco.inventory_service.service;

import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import com.arseniolourenco.inventory_service.model.Inventory;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isInStock(List<String> skuCodes) {
        // Fetch inventory records matching the skuCodes
        List<Inventory> inventoryList = inventoryRepository.findBySkuCodeIn(skuCodes);

        // Map each skuCode to a total quantity by summing duplicates
        Map<String, Integer> skuCodeToTotalQuantity = inventoryList
                .stream()
                .collect(Collectors.toMap(
                        Inventory::getSkuCode,
                        Inventory::getQuantity,
                        Integer::sum // Sum the quantities if duplicates are found
                ));

        // Generate the InventoryResponse list indicating stock status and total quantity
        return skuCodes
                .stream()
                .map(code -> {
                    Integer totalQuantity = skuCodeToTotalQuantity.getOrDefault(code, 0); // Get total quantity or default to 0
                    return new InventoryResponse(
                            code,
                            totalQuantity > 0, // Check if the total quantity is greater than 0 for stock status
                            totalQuantity // Include the total quantity in the response
                    );
                })
                .collect(Collectors.toList());
    }

//    @Transactional
//    public List<InventoryRequest> reduceStock(List<String> skuCodes, List<Integer> quantities) {
//        // Aggregate quantities for each SKU code
//        Map<String, Integer> aggregatedSkuQuantityMap = aggregateSkuQuantities(skuCodes, quantities);
//
//        // Fetch inventory for the SKUs in the request
//        List<Inventory> inventoryList = inventoryRepository.findBySkuCodeIn(skuCodes);
//
//        // Validate SKU existence in the inventory
//        validateSkuExistence(skuCodes, inventoryList);
//
//        // Map current stock levels for SKUs
//        Map<String, Integer> skuStockMap = mapSkuStockLevels(inventoryList);
//
//        // Reduce stock and build updated inventory requests
//        return processStockReduction(aggregatedSkuQuantityMap, inventoryList, skuStockMap);
//    }

//    @Transactional
//    public List<InventoryRequest> reduceStock(InventoryRequest inventoryRequest) {
//        // Extract SKU codes and quantities from the InventoryRequest object
//        List<String> skuCodes = inventoryRequest.getSkuCodes();
//        List<Integer> quantities = inventoryRequest.getQuantities();
//
//        // Aggregate quantities for each SKU code
//        Map<String, Integer> aggregatedSkuQuantityMap = aggregateSkuQuantities(skuCodes, quantities);
//
//        // Fetch inventory for the SKUs in the request
//        List<Inventory> inventoryList = inventoryRepository.findBySkuCodeIn(skuCodes);
//
//        // Validate SKU existence in the inventory
//        validateSkuExistence(skuCodes, inventoryList);
//
//        // Map current stock levels for SKUs
//        Map<String, Integer> skuStockMap = mapSkuStockLevels(inventoryList);
//
//        // Reduce stock and build updated inventory requests
//        return processStockReduction(aggregatedSkuQuantityMap, inventoryList, skuStockMap);
//    }

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
            aggregatedSkuQuantityMap.merge(request.getSkuCode(), request.getQuantity(), Integer::sum);
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
            throw new IllegalArgumentException("SKU Code(s) not found in inventory: " + String.join(", ", missingSkuCodes));
        }
    }

    private Map<String, Integer> mapSkuStockLevels(List<Inventory> inventoryList) {
        Map<String, Integer> skuStockMap = new HashMap<>();
        for (Inventory inventory : inventoryList) {
            skuStockMap.merge(inventory.getSkuCode(), inventory.getQuantity(), Integer::sum);
        }
        return skuStockMap;
    }

    private void processStockReduction(Map<String, Integer> aggregatedSkuQuantityMap, List<Inventory> inventoryList, Map<String, Integer> skuStockMap) {
        for (Map.Entry<String, Integer> entry : aggregatedSkuQuantityMap.entrySet()) {
            String skuCode = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            Integer currentStock = skuStockMap.getOrDefault(skuCode, 0);

            // Ensure enough stock exists
            if (currentStock < requestedQuantity) {
                throw new RuntimeException("Insufficient stock for SKU: " + skuCode + ". Available: " + currentStock + ", Required: " + requestedQuantity);
            }

            // Reduce stock in the inventory for the specified SKU code
            reduceStockForSku(inventoryList, skuCode, requestedQuantity);
        }
    }

    private void reduceStockForSku(List<Inventory> inventoryList, String skuCode, int requestedQuantity) {
        int quantityToReduce = requestedQuantity;
        for (Inventory inventory : inventoryList) {
            if (inventory.getSkuCode().equals(skuCode) && quantityToReduce > 0) {
                Integer availableStock = inventory.getQuantity();
                Integer reduceAmount = Math.min(availableStock, quantityToReduce);
                inventory.setQuantity(availableStock - reduceAmount);
                quantityToReduce -= reduceAmount;
                inventoryRepository.save(inventory);
                log.info("Reduced stock for SKU: {} by {}. Remaining: {}", skuCode, reduceAmount, inventory.getQuantity());
            }

            if (quantityToReduce <= 0) break;
        }

        if (quantityToReduce > 0) {
            throw new RuntimeException("Unable to reduce the full requested quantity for SKU: " + skuCode);
        }
    }
}