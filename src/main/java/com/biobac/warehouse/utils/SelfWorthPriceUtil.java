package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SelfWorthPriceUtil {
    public static BigDecimal calculateIngredientPrice(IngredientBalance balance) {
        // todo add additional expenses for calculating
        List<IngredientDetail> details = balance.getDetails();
        int count = 0;
        BigDecimal result = BigDecimal.ZERO;
        for (IngredientDetail detail : details) {
            if (detail.getPrice() != null && detail.getQuantity() != null) {
                BigDecimal lineTotal = detail.getPrice()
                        .multiply(BigDecimal.valueOf(detail.getQuantity()));
                result = result.add(lineTotal);
                count += detail.getQuantity();
            }
        }
        return result.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateProductPrice() {
        return null;
    }
}
