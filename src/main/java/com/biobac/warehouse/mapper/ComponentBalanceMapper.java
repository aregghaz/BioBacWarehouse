package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.utils.SelfWorthPriceUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Component
public class ComponentBalanceMapper {

    public ComponentBalanceIngResponse toIngResponse(IngredientBalance entity) {
        ComponentBalanceIngResponse response = new ComponentBalanceIngResponse();
        BigDecimal selfWorth = SelfWorthPriceUtil.calculateIngredientPrice(entity);
        response.setId(entity.getId());
        response.setSelfWorthPrice(selfWorth);
        response.setUnitName(entity.getIngredient().getUnit().getName());
        response.setTotalPrice(selfWorth.multiply(BigDecimal.valueOf(entity.getBalance())));
        response.setIngredientName(entity.getIngredient() == null ? null : entity.getIngredient().getName());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setBalance(entity.getBalance());
        assert entity.getIngredient() != null;
        response.setMinimalBalance(
                Optional.of(entity.getIngredient())
                        .map(Ingredient::getMinimalBalance)
                        .orElse(0.0)
        );
        response.setIngredientGroupName(entity.getIngredient().getIngredientGroup().getName());
        response.setExpirationDate(getIngredientLastExpirationDate(entity));
        return response;
    }

    public ComponentBalanceProdResponse toProdResponse(ProductBalance entity) {
        ComponentBalanceProdResponse response = new ComponentBalanceProdResponse();
        BigDecimal selfWorth = SelfWorthPriceUtil.calculateProductPrice(entity);
        response.setId(entity.getId());
        response.setUnitName(entity.getProduct().getUnit().getName());
        response.setSelfWorthPrice(selfWorth);
        response.setTotalPrice(selfWorth.multiply(BigDecimal.valueOf(entity.getBalance())));
        response.setBalance(entity.getBalance());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setProductName(entity.getProduct() == null ? null : entity.getProduct().getName());

        assert entity.getProduct() != null;
        response.setMinimalBalance(
                Optional.of(entity.getProduct())
                        .map(Product::getMinimalBalance)
                        .orElse(0.0)
        );
        response.setProductGroupName(entity.getProduct().getProductGroup().getName());
        response.setExpirationDate(getProductLastExpirationDate(entity));
        return response;
    }

    private <T> LocalDate getLastExpirationDate(List<T> details, Function<T, LocalDate> expirationMapper) {
        LocalDate today = LocalDate.now();

        Optional<LocalDate> lastExpired = details.stream()
                .map(expirationMapper)
                .filter(Objects::nonNull)
                .filter(d -> d.isBefore(today))
                .max(Comparator.naturalOrder());

        return lastExpired.orElseGet(() -> details.stream()
                .map(expirationMapper)
                .filter(Objects::nonNull)
                .filter(d -> !d.isBefore(today))
                .min(Comparator.naturalOrder())
                .orElse(null));
    }

    private LocalDate getProductLastExpirationDate(ProductBalance componentBalance) {
        return getLastExpirationDate(componentBalance.getDetails(), ProductDetail::getExpirationDate);
    }

    private LocalDate getIngredientLastExpirationDate(IngredientBalance ingredientBalance) {
        return getLastExpirationDate(ingredientBalance.getDetails(), IngredientDetail::getExpirationDate);
    }
}
