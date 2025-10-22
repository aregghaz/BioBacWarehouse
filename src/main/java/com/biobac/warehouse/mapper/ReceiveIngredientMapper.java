package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientUnitType;
import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

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

        for (IngredientUnitType utc : ingredient.getUnitTypeConfigs()) {
            UnitTypeCalculatedResponse c = new UnitTypeCalculatedResponse();
            if (utc.getUnitType() != null) {
                c.setUnitTypeName(utc.getUnitType().getName());
            }
            c.setUnitTypeId(utc.getId());
            c.setBaseUnit(utc.isBaseType());

            if (utc.isBaseType()) {
                c.setSize(qty);
            } else {
                double utSize = Optional.ofNullable(utc.getSize()).orElse(0.0);
                c.setSize(utSize == 0.0 ? qty : Math.ceil(qty / utSize));
            }

            list.add(c);
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
