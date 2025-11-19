package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IngredientHistorySingleResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientGroupName;
    private String unitName;
    private Long warehouseId;
    private String warehouseName;
    private Long actionId;
    private String actionName;
    private boolean increase;
    private Double quantityChange;
    private Double quantityResult;
    private String username;
    private String notes;
    private LocalDateTime timestamp;
}
