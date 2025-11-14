package com.biobac.warehouse.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ManufactureCalculateResponse {
    private Double requiredQuantity;
    private Double balanceQuantity;
    private BigDecimal requiredPrice;
    private BigDecimal availablePrice;
    private String componentName;
    private Long ingredientId;
    private Long productId;
}
