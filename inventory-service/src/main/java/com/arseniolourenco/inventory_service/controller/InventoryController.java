package com.arseniolourenco.inventory_service.controller;

import com.arseniolourenco.inventory_service.dto.InventoryRequest;
import com.arseniolourenco.inventory_service.dto.InventoryResponse;
import com.arseniolourenco.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<InventoryResponse>> isInStock(@RequestParam List<String> skuCode) {
        log.info("Checking inventory for SKUs: {}", skuCode);
        return ResponseEntity.ok(inventoryService.isInStock(skuCode));
    }


//    @PostMapping("/reduce")
//    @ResponseStatus(HttpStatus.OK)
//    public List<InventoryRequest> reduceInventory(
//            @RequestParam List<String> skuCode,
//            @RequestParam List<Integer> quantity) {
//        return inventoryService.reduceStock(skuCode, quantity);
//    }

    @PostMapping("/reduce")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> reduceInventory(@RequestBody List<InventoryRequest> inventoryRequests) {
        try {
            inventoryService.reduceStock(inventoryRequests);
            return ResponseEntity.ok("Inventory reduced successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error reducing inventory: " + e.getMessage());
        }
    }

}