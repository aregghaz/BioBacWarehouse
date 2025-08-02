package com.biobac.warehouse.dto;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InventoryItemDto {
    private Long id;
    private Long productId;
    private Long ingredientId;
    private Long warehouseId;
    private Integer quantity;
    private LocalDate lastUpdated;
}
