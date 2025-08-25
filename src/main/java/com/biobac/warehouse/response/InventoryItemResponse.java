package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InventoryItemResponse {
    private Long id;
    private Long productId;
    private Long ingredientId;
    private Long ingredientGroupId;
    private Long warehouseId;
    private Integer quantity;
    private Integer ingredientCount;
    private LocalDate lastUpdated;
}
