package com.arseniolourenco.inventory_service;

import com.arseniolourenco.inventory_service.model.Inventory;
import com.arseniolourenco.inventory_service.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
        return args -> {
            Inventory inventory1 = new Inventory();
            inventory1.setSkuCode("iPhone 15");
            inventory1.setQuantity(10);

            Inventory inventory2 = new Inventory();
            inventory2.setSkuCode("iPhone 16");
            inventory2.setQuantity(10);

            Inventory inventory3 = new Inventory();
            inventory3.setSkuCode("iPhone 12");
            inventory3.setQuantity(10);

            inventoryRepository.save(inventory1);
            inventoryRepository.save(inventory2);
            inventoryRepository.save(inventory3);
        };

    }

}
