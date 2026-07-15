package com.arseniolourenco.order_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNumber;
    private String status;
    private String message;
    @OneToMany(cascade = CascadeType.ALL,  fetch = FetchType.EAGER)
    private List<OrderLineItems> orderLineItemsList;


}
