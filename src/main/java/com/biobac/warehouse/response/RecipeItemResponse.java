package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeItemResponse {
    private Long id;
    private String name;
    private String notes;
    private List<RecipeComponentDto> components;

    @Getter
    @Setter
    public static class RecipeComponentDto {
        private Long ingredientId;
        private String ingredientName;
        private Double quantity;
        private String notes;
    }
}
