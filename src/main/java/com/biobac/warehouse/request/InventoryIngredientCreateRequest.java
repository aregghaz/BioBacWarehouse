package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InventoryIngredientCreateRequest {
    private Long warehouseId;
    private Long ingredientId;
    private Long companyId;
    private LocalDate receiptDay;;
    private List<InventoryUnitTypeRequest> unitTypes;
}
