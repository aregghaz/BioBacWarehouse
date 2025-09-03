package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryIngredientCreateRequest {
    private Long warehouseId;
    private Long ingredientId;
    private Long companyId;
    private Double quantity;
}
