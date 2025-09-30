package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentInventorySelection {
    private Long ingredientId;
    private Long productId;
    private Long inventoryItemId;
}