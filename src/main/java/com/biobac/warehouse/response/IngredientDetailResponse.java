package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class IngredientDetailResponse {
    private String ingredientName;

    private LocalDate importDate;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private Double quantity;

    private BigDecimal price;
}
