package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryItemResponse {
    private Long id;
    private Double quantity;
    private String warehouseName;
    private Long warehouseId;
    private String productName;
    private String ingredientName;
    private String unitName;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
