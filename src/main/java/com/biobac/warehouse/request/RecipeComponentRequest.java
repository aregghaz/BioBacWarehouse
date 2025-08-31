package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeComponentRequest {
    private Long ingredientId;
    private Long productId;
    private Double quantity;
}
