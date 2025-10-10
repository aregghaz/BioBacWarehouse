package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IngredientHistoryResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private boolean increase;
    private Double quantityChange;
    private Double quantityResult;
    private String username;
    private String notes;
    private LocalDateTime createdAt;
}
