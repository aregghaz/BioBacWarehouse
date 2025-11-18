package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.ReceiveExpense;
import com.biobac.warehouse.entity.ReceiveIngredient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class ReceiveIngredientCalculationUtil {

    private ReceiveIngredientCalculationUtil() {
    }

    /**
     * Calculates the total additional expenses from a list of ReceiveExpense entities.
     *
     * @param expenses list of expenses (can be null)
     * @return total additional expense amount
     */
    public static BigDecimal calculateTotalExpenses(List<ReceiveExpense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return expenses.stream()
                .map(ReceiveExpense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total base cost of all receive ingredients (price × quantity).
     *
     * @param receiveIngredients list of receive ingredient items
     * @return total base cost
     */
    public static BigDecimal calculateTotalBaseCost(List<ReceiveIngredient> receiveIngredients) {
        if (receiveIngredients == null || receiveIngredients.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return receiveIngredients.stream()
                .map(item -> {
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    double quantity = item.getQuantity() != null ? item.getQuantity() : 0.0;
                    return price.multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}