package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredientHistoryResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private String IngredientGroupName;
    private String unitName;
    private Double initialCount;
    private Double eventualCount;
}
