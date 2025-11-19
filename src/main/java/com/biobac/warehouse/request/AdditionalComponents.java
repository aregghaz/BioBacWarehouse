package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdditionalComponents {
    private Long ingredientId;
    private Long productId;
    private Long inventoryItemId;
    private List<InventoryUnitTypeRequest> unitTypes;
}
