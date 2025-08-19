package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InventoryItemTableResponse {
    private Long id;
    private String productName;
    private String ingredientName;
    private String ingredientGroupName;
    private String warehouseName;
    private Integer quantity;
    private Integer ingredientCount;
    private LocalDate lastUpdated;
}
