package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientUnitType;
import com.biobac.warehouse.entity.ReceiveGroup;
import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.ReceiveIngredientRequest;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.response.ReceiveIngredientsPriceCalcResponse;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class ReceiveIngredientMapper {

    @Autowired
    protected CompanyClient companyClient;

    @Mapping(source = "warehouse.name", target = "warehouseName")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.unit.name", target = "unitName")
    @Mapping(source = "status.name", target = "status")
    @Mapping(source = "status.id", target = "statusId")
    @Mapping(source = "group.id", target = "groupId")
    public abstract ReceiveIngredientResponse toSingleResponse(ReceiveIngredient item);

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "unitName", source = "ingredient.unit.name")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "calculatedPrice", source = "calculateTotalPrice")
    @Mapping(target = "total", source = "total")
    public abstract ReceiveIngredientsPriceCalcResponse.Ingredients toIngredientResponse(
            Ingredient ingredient,
            Double quantity,
            BigDecimal price,
            BigDecimal calculateTotalPrice,
            BigDecimal total
    );

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "group", source = "group")
    @Mapping(target = "ingredient", source = "ingredient")
    @Mapping(target = "warehouse", source = "warehouse")
    @Mapping(target = "companyId", source = "request.companyId")
    @Mapping(target = "quantity", source = "request.quantity")
    @Mapping(target = "price", source = "request.price")
    @Mapping(target = "importDate", ignore = true)
    @Mapping(target = "manufacturingDate", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "receivedQuantity", constant = "0.0")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "lastPrice", ignore = true)
    @Mapping(target = "detail", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    public abstract ReceiveIngredient toReceiveIngredient(
            ReceiveIngredientRequest request,
            ReceiveGroup group,
            Ingredient ingredient,
            Warehouse warehouse
    );

    @AfterMapping
    protected void scaleValues(
            @MappingTarget ReceiveIngredientsPriceCalcResponse.Ingredients dto,
            BigDecimal price,
            BigDecimal calculatedTotalPrice,
            BigDecimal total
    ) {
        dto.setPrice(price.setScale(2, RoundingMode.HALF_EVEN));
        dto.setCalculatedPrice(calculatedTotalPrice.setScale(2, RoundingMode.HALF_EVEN));
        dto.setTotal(total.setScale(2, RoundingMode.HALF_EVEN));
    }

    @AfterMapping
    protected void afterMapCompany(ReceiveIngredient item, @MappingTarget ReceiveIngredientResponse resp) {
        if (item == null || resp == null) return;
        Long cid = item.getCompanyId();
        resp.setCompanyId(cid);
        if (cid != null) {
            try {
                ApiResponse<String> api = companyClient.getCompanyName(cid);
                if (api != null && Boolean.TRUE.equals(api.getSuccess())) {
                    resp.setCompanyName(api.getData());
                } else if (api != null && api.getData() != null) {
                    resp.setCompanyName(api.getData());
                }
            } catch (Exception ignored) {
            }
        }
    }

    @AfterMapping
    protected void afterCalculateReceivedUnitTypes(ReceiveIngredient item, @MappingTarget ReceiveIngredientResponse resp) {
        calculateUnitTypes(item, resp, true);
    }

    @AfterMapping
    protected void afterCalculateUnitTypes(ReceiveIngredient item, @MappingTarget ReceiveIngredientResponse resp) {
        calculateUnitTypes(item, resp, false);
    }

    private void calculateUnitTypes(ReceiveIngredient item, ReceiveIngredientResponse resp, boolean isReceived) {
        if (item == null || resp == null) return;

        Ingredient ingredient = item.getIngredient();
        if (ingredient == null || ingredient.getUnitTypeConfigs() == null) {
            if (isReceived) resp.setReceivedUnitTypeConfigs(List.of());
            else resp.setUnitTypeConfigs(List.of());
            return;
        }

        double qty = Optional.ofNullable(isReceived ? item.getReceivedQuantity() : item.getQuantity()).orElse(0.0);
        List<UnitTypeCalculatedResponse> list = new ArrayList<>();

        for (IngredientUnitType ingredientUnitType : ingredient.getUnitTypeConfigs()) {
            UnitTypeCalculatedResponse unitTypeCalculatedResponse = new UnitTypeCalculatedResponse();
            if (ingredientUnitType.getUnitType() != null) {
                unitTypeCalculatedResponse.setUnitTypeName(ingredientUnitType.getUnitType().getName());
            }
            unitTypeCalculatedResponse.setUnitTypeId(ingredientUnitType.getId());
            unitTypeCalculatedResponse.setBaseUnit(ingredientUnitType.isBaseType());

            if (ingredientUnitType.isBaseType()) {
                unitTypeCalculatedResponse.setSize(qty);
            } else {
                double utSize = Optional.ofNullable(ingredientUnitType.getSize()).orElse(0.0);
                unitTypeCalculatedResponse.setSize(utSize == 0.0 ? qty : Math.ceil(qty / utSize));
            }

            list.add(unitTypeCalculatedResponse);
        }

        if (isReceived) resp.setReceivedUnitTypeConfigs(list);
        else resp.setUnitTypeConfigs(list);
    }

    @AfterMapping
    protected void afterSetSucceed(ReceiveIngredient item, @MappingTarget ReceiveIngredientResponse resp) {
        if (item == null || resp == null) return;
        if (item.getStatus() != null && item.getStatus().getName() != null) {
            resp.setSucceed("завершенные".equals(item.getStatus().getName()));
        } else {
            resp.setSucceed(false);
        }
    }
}
