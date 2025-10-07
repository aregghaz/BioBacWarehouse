package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientDetail;
import com.biobac.warehouse.entity.ProductBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public class SelfWorthPriceUtil {
    private static BigDecimal getLastPrice(List<IngredientDetail> details) {
        return details.stream()
                .filter(d -> d.getImportDate() != null && d.getPrice() != null)
                .max(Comparator.comparing(IngredientDetail::getImportDate))
                .map(IngredientDetail::getPrice).get();
    }

    public static BigDecimal calculateIngredientPrice(IngredientBalance balance) {
        // todo add additional expenses for calculating
        List<IngredientDetail> details = balance.getDetails();
        int count = 0;
        BigDecimal result = BigDecimal.ZERO;
        for (IngredientDetail detail : details) {
            if (detail.getQuantity() > 0) {
                BigDecimal lineTotal = detail.getPrice()
                        .multiply(BigDecimal.valueOf(detail.getQuantity()));
                result = result.add(lineTotal);
                count += detail.getQuantity();
                result = result.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
            } else {
                result = getLastPrice(details);
            }
        }
        return result;
    }

    public static BigDecimal calculateProductPrice(ProductBalance balance) {
        return null;
    }
}
