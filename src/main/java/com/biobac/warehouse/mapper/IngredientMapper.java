package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.AttributeTargetType;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class IngredientMapper {

    @Autowired
    protected CompanyClient companyClient;

    @Autowired
    protected AttributeClient attributeClient;

    public IngredientResponse toResponse(Ingredient ingredient) {
        if (ingredient == null) return null;
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setExpiration(ingredient.getExpiration());
        response.setPrice(ingredient.getPrice());
        response.setMinimalBalance(ingredient.getMinimalBalance());
        response.setDefaultWarehouseName(ingredient.getDefaultWarehouse().getName());
        response.setDefaultWarehouseId(ingredient.getDefaultWarehouse().getId());
        response.setAttributeGroupIds(ingredient.getAttributeGroupIds());
        if (ingredient.getIngredientGroup() != null) {
            response.setIngredientGroupId(ingredient.getIngredientGroup().getId());
            response.setIngredientGroupName(ingredient.getIngredientGroup().getName());
        }
        response.setCreatedAt(ingredient.getCreatedAt());
        response.setUpdatedAt(ingredient.getUpdatedAt());

        try {
            if (ingredient.getId() != null) {
                ApiResponse<List<AttributeResponse>> apiResponse = attributeClient.getValues(ingredient.getId(), AttributeTargetType.INGREDIENT.name());
                response.setAttributes(apiResponse.getData());
            }
        } catch (Exception ignored) {
        }

        if (ingredient.getHistories() != null && !ingredient.getHistories().isEmpty()) {
            IngredientHistory lastHistory = ingredient.getHistories().stream()
                    .filter(h -> h.getCreatedAt() != null)
                    .max(Comparator.comparing(IngredientHistory::getCreatedAt))
                    .orElseGet(() -> ingredient.getHistories().stream()
                            .filter(h -> h.getLastPrice() != null || h.getCompanyId() != null)
                            .findFirst()
                            .orElse(null));

            if (lastHistory != null) {
                try {
                    ApiResponse<String> resp = companyClient.getCompanyName(lastHistory.getCompanyId());
                    if (resp != null && Boolean.TRUE.equals(resp.getSuccess())) {
                        response.setLastCompanyName(resp.getData());
                    } else if (resp != null && resp.getData() != null) {
                        response.setLastCompanyName(resp.getData());
                    }
                } catch (Exception ignored) {
                }

                response.setLastPrice(lastHistory.getLastPrice());
                response.setLastCompanyId(lastHistory.getCompanyId());
            }
        }

        if (ingredient.getUnit() != null) {
            response.setUnitId(ingredient.getUnit().getId());
            response.setUnitName(ingredient.getUnit().getName());
        }

        response.setTotalQuantity(null);

        if (ingredient.getUnitTypeConfigs() != null) {
            List<UnitTypeConfigResponse> cfgs = ingredient.getUnitTypeConfigs().stream().map(cfg -> {
                UnitTypeConfigResponse r = new UnitTypeConfigResponse();
                r.setId(cfg.getId());
                if (cfg.getUnitType() != null) {
                    r.setUnitTypeId(cfg.getUnitType().getId());
                    r.setUnitTypeName(cfg.getUnitType().getName());
                }
                r.setBaseUnit(cfg.isBaseType());
                r.setSize(cfg.getSize());
                r.setCreatedAt(cfg.getCreatedAt());
                r.setUpdatedAt(cfg.getUpdatedAt());
                return r;
            }).toList();
            response.setUnitTypeConfigs(cfgs);
        }

        return response;
    }

}
