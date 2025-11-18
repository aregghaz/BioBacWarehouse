package com.biobac.warehouse.utils;

import com.biobac.warehouse.dto.IngredientPriceRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class IngredientPriceUtil {

    private IngredientPriceUtil() {
    }

    public static IngredientPriceRecord calculateIngredientPrice(
            BigDecimal price,
            Double qty,
            BigDecimal totalExpenses,
            BigDecimal totalBase
    ) {
        BigDecimal safePrice = price != null ? price : BigDecimal.ZERO;
        double safeQty = qty != null ? qty : 0.0;

        BigDecimal itemBaseAmount = safePrice.multiply(BigDecimal.valueOf(safeQty));

        BigDecimal additionalPerUnit = BigDecimal.ZERO;

        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0 &&
                totalBase.compareTo(BigDecimal.ZERO) > 0 &&
                safeQty > 0.0) {

            additionalPerUnit = itemBaseAmount
                    .divide(totalBase, 8, RoundingMode.HALF_EVEN)
                    .multiply(totalExpenses)
                    .divide(BigDecimal.valueOf(safeQty), 2, RoundingMode.HALF_EVEN);
        }

        BigDecimal calculatedPrice = safePrice.add(additionalPerUnit);
        BigDecimal total = calculatedPrice.multiply(BigDecimal.valueOf(safeQty)).setScale(2, RoundingMode.HALF_EVEN);

        return new IngredientPriceRecord(safePrice, calculatedPrice, total);
    }
}
