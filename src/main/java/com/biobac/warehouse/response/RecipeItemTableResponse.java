package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeItemTableResponse {
    private Long id;
    private String productName;
    private String ingredientName;
    private String ingredientUnit;
    private Double quantity;
    private String notes;
}
