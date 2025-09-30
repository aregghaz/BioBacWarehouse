package com.biobac.warehouse.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComponentInventorySelection {
    private Long ingredientId;
    private Long productId;
    private Long inventoryItemId;
    @NotEmpty(message = "At least one unit type is required")
    private List<@Valid InventoryUnitTypeRequest> unitTypes;
}