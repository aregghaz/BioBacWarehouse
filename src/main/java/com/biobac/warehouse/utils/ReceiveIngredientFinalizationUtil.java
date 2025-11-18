package com.biobac.warehouse.utils;

import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientDetail;
import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.repository.IngredientBalanceRepository;
import com.biobac.warehouse.repository.IngredientDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class ReceiveIngredientFinalizationUtil {

    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final IngredientBalanceUtil ingredientBalanceUtil;

    public void finalizeReceiveIngredient(
            ReceiveIngredient current,
            Double receivedQuantity,
            Warehouse warehouse,
            Ingredient ingredient,
            BigDecimal additionalExpense,
            BigDecimal totalBaseCost) {

        double delta = receivedQuantity != null ? receivedQuantity : 0.0;
        IngredientBalance balance = updateIngredientBalance(warehouse, ingredient, delta);
        BigDecimal detailPrice = calculateFinalPrice(current, additionalExpense, totalBaseCost);
        IngredientDetail detail = createOrUpdateIngredientDetail(current, balance, detailPrice, delta, ingredient);

        current.setDetail(detail);

    }

    private IngredientBalance updateIngredientBalance(
            Warehouse warehouse,
            Ingredient ingredient,
            double delta) {

        IngredientBalance balance = ingredientBalanceUtil.getOrCreateIngredientBalance(warehouse, ingredient);
        double currentBalance = balance.getBalance() != null ? balance.getBalance() : 0.0;
        balance.setBalance(currentBalance + delta);
        return ingredientBalanceRepository.save(balance);
    }

    /**
     * Calculates the final unit price including proportionally distributed additional expenses.
     *
     * Formula: finalPrice = basePrice + (itemBaseAmount / totalBase) * totalExpense / quantity
     */
    private BigDecimal calculateFinalPrice(
            ReceiveIngredient current,
            BigDecimal additionalExpense,
            BigDecimal totalBaseCost) {

        BigDecimal basePrice = current.getPrice() != null ? current.getPrice() : BigDecimal.ZERO;
        BigDecimal plannedQty = BigDecimal.valueOf(current.getQuantity() != null ? current.getQuantity() : 0.0);
        BigDecimal itemBaseAmount = basePrice.multiply(plannedQty);

        BigDecimal additionalPerUnit = calculateAdditionalCostPerUnit(
                itemBaseAmount, plannedQty, additionalExpense, totalBaseCost);

        return basePrice.add(additionalPerUnit);
    }

    /**
     * Calculates the additional cost per unit from distributed expenses.
     */
    private BigDecimal calculateAdditionalCostPerUnit(
            BigDecimal itemBaseAmount,
            BigDecimal quantity,
            BigDecimal totalExpenses,
            BigDecimal totalBaseCost) {

        if (totalExpenses.compareTo(BigDecimal.ZERO) <= 0
                || totalBaseCost.compareTo(BigDecimal.ZERO) <= 0
                || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return itemBaseAmount
                .divide(totalBaseCost, 8, RoundingMode.HALF_EVEN)
                .multiply(totalExpenses)
                .divide(quantity, 2, RoundingMode.HALF_EVEN);
    }

    /**
     * Creates a new IngredientDetail or updates existing one with received quantity and calculated price.
     */
    private IngredientDetail createOrUpdateIngredientDetail(
            ReceiveIngredient current,
            IngredientBalance balance,
            BigDecimal detailPrice,
            double delta,
            Ingredient ingredient) {

        IngredientDetail detail = current.getDetail();
        if (detail == null) {
            detail = new IngredientDetail();
            detail.setReceiveIngredient(current);
            detail.setQuantity(0.0);
        }

        detail.setIngredientBalance(balance);
        detail.setPrice(detailPrice);
        detail.setImportDate(current.getImportDate());
        detail.setManufacturingDate(current.getManufacturingDate());

        // Calculate expiration date
        if (current.getManufacturingDate() != null && ingredient != null && ingredient.getExpiration() != null) {
            detail.setExpirationDate(current.getManufacturingDate().plusDays(ingredient.getExpiration()));
        }

        double currentDetailQty = detail.getQuantity() != null ? detail.getQuantity() : 0.0;
        detail.setQuantity(currentDetailQty + delta);

        return ingredientDetailRepository.save(detail);
    }
}
