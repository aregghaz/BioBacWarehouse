package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryIngredientCreateRequest {
    private Long warehouseId;
    private Long ingredientId;
    private Long ingredientGroupId;
    private Double quantity;
    // Optional unit for this inventory record
    private Long unitId;
}
