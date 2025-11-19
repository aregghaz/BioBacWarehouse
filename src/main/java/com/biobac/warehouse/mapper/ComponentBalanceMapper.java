package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.utils.SelfWorthPriceUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        response.setIngredientUnitName(entity.getIngredient().getUnit().getName());
        response.setTotalPrice(selfWorth.multiply(BigDecimal.valueOf(entity.getBalance())));
        response.setIngredientName(entity.getIngredient() == null ? null : entity.getIngredient().getName());
        response.setIngredientId(entity.getIngredient() == null ? null : entity.getIngredient().getId());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setBalance(entity.getBalance());
        assert entity.getIngredient() != null;
        response.setIngredientMinimalBalance(
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
        response.setProductUnitName(entity.getProduct().getUnit().getName());
        response.setSelfWorthPrice(selfWorth);
        response.setTotalPrice(selfWorth.multiply(BigDecimal.valueOf(entity.getBalance())));
        response.setBalance(entity.getBalance());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setProductName(entity.getProduct() == null ? null : entity.getProduct().getName());

        assert entity.getProduct() != null;
        response.setProductMinimalBalance(
                Optional.of(entity.getProduct())
                        .map(Product::getMinimalBalance)
                        .orElse(0.0)
        );
        response.setProductGroupName(entity.getProduct().getProductGroup().getName());
        response.setProductExpirationDate(getProductLastExpirationDate(entity));
        return response;
    }

    private <T> LocalDateTime getLastExpirationDate(List<T> details, Function<T, LocalDateTime> expirationMapper) {
        LocalDateTime today = LocalDateTime.now();

        Optional<LocalDateTime> lastExpired = details.stream()
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

    private LocalDateTime getProductLastExpirationDate(ProductBalance componentBalance) {
        return getLastExpirationDate(componentBalance.getDetails(), ProductDetail::getExpirationDate);
    }

    private LocalDateTime getIngredientLastExpirationDate(IngredientBalance ingredientBalance) {
        return getLastExpirationDate(ingredientBalance.getDetails(), IngredientDetail::getExpirationDate);
    }
}
