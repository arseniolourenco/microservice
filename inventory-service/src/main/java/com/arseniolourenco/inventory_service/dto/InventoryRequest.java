//package com.arseniolourenco.inventory_service.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.List;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class InventoryRequest {
//    private List<String> skuCodes;
//    private List<Integer> quantities;
//}

package com.arseniolourenco.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
    private String skuCode;
    private Integer quantity;
}