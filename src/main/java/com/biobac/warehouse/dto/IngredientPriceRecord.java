package com.biobac.warehouse.dto;

import java.math.BigDecimal;

public record IngredientPriceRecord(
       BigDecimal price,
       BigDecimal calculatedPrice,
       BigDecimal total
) {
}
