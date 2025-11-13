package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManufactureCalculateResponse {
    private Double requiredQuantity;
    private Double balanceQuantity;
    private BigDecimal price;
    private String componentName;
    private Long ingredientId;
    private Long productId;
}
