package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RecipeItemResponse {
    private Long id;
    private String name;
    private String notes;
    private List<RecipeComponentDto> components;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class RecipeComponentDto {
        private Long ingredientId;
        private String ingredientName;
        private Long productId;
        private String productName;
        private Double quantity;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
