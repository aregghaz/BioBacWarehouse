package com.biobac.warehouse.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReceiveIngredientsPriceCalcRequest {
    private Long ingredientId;
    private Double quantity;
    private BigDecimal price;
}
