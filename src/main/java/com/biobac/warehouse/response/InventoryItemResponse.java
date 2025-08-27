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
    private String productName;
    private String ingredientName;
    private String ingredientGroupName;
    private Long unitId;
    private String unitName;
    private LocalDateTime lastUpdated;
}
