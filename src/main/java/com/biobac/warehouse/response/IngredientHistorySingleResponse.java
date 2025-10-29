package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IngredientHistorySingleResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String ingredientGroupName;
    private String unitName;
    private boolean increase;
    private Double quantityChange;
    private Double quantityResult;
    private String username;
    private String notes;
    private LocalDate timestamp;
}
