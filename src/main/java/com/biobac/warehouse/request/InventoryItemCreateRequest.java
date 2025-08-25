package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InventoryItemCreateRequest {
    private Long productId;
    private Long ingredientId;
    private Long ingredientGroupId;
    private Long warehouseId;
    private Integer quantity;
    private Integer ingredientCount;
    private LocalDate lastUpdated;
}
