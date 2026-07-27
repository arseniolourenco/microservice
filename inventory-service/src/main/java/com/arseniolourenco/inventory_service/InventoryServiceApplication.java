package com.arseniolourenco.inventory_service;

import com.arseniolourenco.inventory_service.model.InventoryModel;
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
            if (inventoryRepository.count() == 0) {
                InventoryModel inventory1 = new InventoryModel();
                inventory1.setSkuCode("IPHONE15");
                inventory1.setQuantity(10);

                InventoryModel inventory2 = new InventoryModel();
                inventory2.setSkuCode("IPHONE16");
                inventory2.setQuantity(10);

                InventoryModel inventory3 = new InventoryModel();
                inventory3.setSkuCode("IPHONE17");
                inventory3.setQuantity(10);

                inventoryRepository.save(inventory1);
                inventoryRepository.save(inventory2);
                inventoryRepository.save(inventory3);
            }
        };
    }

}
