package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class IngredientDetailResponse {
    private String ingredientDetailName;
    private LocalDateTime importDate;
    private LocalDateTime expirationDate;
    private LocalDateTime manufacturingDate;
    private Double quantity;
    private BigDecimal price;
    private String ingredientDetailUnitName;
}
