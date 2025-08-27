package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryProductCreateRequest {
    private Long warehouseId;
    private Long productId;
    private Double quantity;
    // Optional unit for this inventory record
    private Long unitId;
}
