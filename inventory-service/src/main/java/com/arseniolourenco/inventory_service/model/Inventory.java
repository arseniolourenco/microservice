package com.arseniolourenco.inventory_service.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku_code", unique = true, nullable = false)
    private String skuCode;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

}