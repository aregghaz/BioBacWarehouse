package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.utils.SelfWorthPriceUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class ComponentBalanceMapper {

    public ComponentBalanceIngResponse toIngResponse(IngredientBalance entity) {
        ComponentBalanceIngResponse response = new ComponentBalanceIngResponse();
        BigDecimal selfWorth = SelfWorthPriceUtil.calculateIngredientPrice(entity);
        response.setId(entity.getId());
        response.setSelfWorthPrice(selfWorth);
        response.setTotalPrice(selfWorth.multiply(BigDecimal.valueOf(entity.getBalance())));
        response.setIngredientName(entity.getIngredient().getName());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setBalance(entity.getBalance());
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
        response.setId(entity.getId());
        response.setProductName(entity.getProduct().getName());
        response.setBalance(entity.getBalance());
        response.setWarehouseName(entity.getWarehouse() == null ? null : entity.getWarehouse().getName());
        response.setMinimalBalance(
                Optional.of(entity.getProduct())
                        .map(Product::getMinimalBalance)
                        .orElse(0.0)
        );
        response.setProductGroupName(entity.getProduct().getProductGroup().getName());
        response.setExpirationDate(getProductLastExpirationDate(entity));
        return response;
    }

    private LocalDate getProductLastExpirationDate(ProductBalance componentBalance) {
        List<ProductDetail> details = componentBalance.getDetails();

        LocalDate today = LocalDate.now();

        return details.stream()
                .map(ProductDetail::getExpirationDate)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(d ->
                        Math.abs(ChronoUnit.DAYS.between(today, d))
                ))
                .orElse(null);
    }

    private LocalDate getIngredientLastExpirationDate(IngredientBalance componentBalance) {
        List<IngredientDetail> details = componentBalance.getDetails();

        LocalDate today = LocalDate.now();

        return details.stream()
                .map(IngredientDetail::getExpirationDate)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(d ->
                        Math.abs(ChronoUnit.DAYS.between(today, d))
                ))
                .orElse(null);
    }
}
