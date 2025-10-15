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

import java.util.List;

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
    protected void afterCalculateUnitTypes(ReceiveIngredient item, @MappingTarget ReceiveIngredientResponse resp) {
        if (item == null || resp == null) return;
        Ingredient ing = item.getIngredient();
        Double qtyObj = item.getQuantity();
        double qty = qtyObj != null ? qtyObj : 0.0;
        if (ing == null || ing.getUnitTypeConfigs() == null) {
            resp.setUnitTypeConfigs(java.util.List.of());
            return;
        }
        List<UnitTypeCalculatedResponse> list = new java.util.ArrayList<>();
        for (IngredientUnitType utc : ing.getUnitTypeConfigs()) {
            UnitTypeCalculatedResponse c = new UnitTypeCalculatedResponse();
            if (utc.getUnitType() != null) {
                c.setUnitTypeName(utc.getUnitType().getName());
            }
            c.setUnitTypeId(utc.getId());
            c.setBaseUnit(utc.isBaseType());
            if (utc.isBaseType()) {
                c.setSize(qty);
            } else {
                Double utSize = utc.getSize();
                if (utSize == null || utSize == 0.0) {
                    c.setSize(qty);
                } else {
                    c.setSize(Math.ceil(qty / utSize));
                }
            }
            list.add(c);
        }
        resp.setUnitTypeConfigs(list);
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
