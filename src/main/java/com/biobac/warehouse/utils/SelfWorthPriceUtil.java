package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientDetail;
import com.biobac.warehouse.entity.ProductBalance;
import com.biobac.warehouse.entity.ProductDetail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public class SelfWorthPriceUtil {
    private static BigDecimal getLastIngredientPrice(List<IngredientDetail> details) {
        return details.stream()
                .filter(d -> d.getImportDate() != null && d.getPrice() != null)
                .max(Comparator.comparing(IngredientDetail::getImportDate))
                .map(IngredientDetail::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal getLastProductPrice(List<ProductDetail> details) {
        return details.stream()
                .filter(d -> d.getManufacturingDate() != null && d.getPrice() != null)
                .max(Comparator.comparing(ProductDetail::getManufacturingDate))
                .map(ProductDetail::getPrice)
                .orElse(BigDecimal.ZERO);
    }

    public static BigDecimal calculateIngredientPrice(IngredientBalance balance) {
        List<IngredientDetail> details = balance.getDetails();
        if (details == null || details.isEmpty()) {
            return balance.getIngredient().getPrice();
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (IngredientDetail detail : details) {
            if (detail.getQuantity() > 0 && detail.getPrice() != null) {
                BigDecimal qty = BigDecimal.valueOf(detail.getQuantity());
                totalValue = totalValue.add(detail.getPrice().multiply(qty));
                totalQuantity = totalQuantity.add(qty);
            }
        }

        if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
        } else {
            return getLastIngredientPrice(details);
        }
    }

    public static BigDecimal calculateProductPrice(ProductBalance balance) {
        // todo add additional expenses for calculating
        List<ProductDetail> details = balance.getDetails();

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;

        for (ProductDetail detail : details) {
            if (detail.getQuantity() > 0 && detail.getPrice() != null) {
                BigDecimal qty = BigDecimal.valueOf(detail.getQuantity());
                totalValue = totalValue.add(detail.getPrice().multiply(qty));
                totalQuantity = totalQuantity.add(qty);
            }
        }

        if (totalQuantity.compareTo(BigDecimal.ZERO) > 0) {
            return totalValue.divide(totalQuantity, 2, RoundingMode.HALF_UP);
        } else {
            return getLastProductPrice(details);
        }
    }
}
