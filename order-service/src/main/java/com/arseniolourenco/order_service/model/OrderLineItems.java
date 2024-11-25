package com.arseniolourenco.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_order_line_items")
public class OrderLineItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;

}
