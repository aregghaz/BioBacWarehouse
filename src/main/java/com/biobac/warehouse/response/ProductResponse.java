package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private Double totalQuantity;
    private String recipeItemName;
    private Long companyId;
    private List<InventoryItemResponse> inventoryItems;
}
