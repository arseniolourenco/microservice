package com.arseniolourenco.inventory_service.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_inventory")
@Builder
public class InventoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;
    @Column(nullable = false)
    private Integer quantity;
    @Version
    private Long version;   //  Optimistic Locking

}