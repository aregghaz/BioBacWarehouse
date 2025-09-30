package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.AttributeTargetType;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.repository.IngredientHistoryRepository;
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

    @Autowired
    IngredientHistoryRepository ingredientHistoryRepository;

    public IngredientResponse toResponse(Ingredient ingredient) {
        if (ingredient == null) return null;
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setExpiration(ingredient.getExpiration());
        response.setPrice(ingredient.getPrice());
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
                    .max(Comparator.comparing(IngredientHistory::getCreatedAt))
                    .orElse(null);
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

        if (ingredient.getUnit() != null) {
            response.setUnitId(ingredient.getUnit().getId());
            response.setUnitName(ingredient.getUnit().getName());
        }

        double totalQuantity = ingredient.getInventoryItems()
                .stream()
                .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                .sum();

        List<InventoryItemResponse> inventoryResponses = ingredient.getInventoryItems().stream()
                .map(item -> mapInventoryItem(item, ingredient.getName(), null))
                .toList();
        response.setTotalQuantity(totalQuantity);
        response.setInventoryItems(inventoryResponses);

        if (ingredient.getUnitTypeConfigs() != null) {
            List<UnitTypeConfigResponse> cfgs = ingredient.getUnitTypeConfigs().stream().map(cfg -> {
                UnitTypeConfigResponse r = new UnitTypeConfigResponse();
                r.setId(cfg.getId());
                if (cfg.getUnitType() != null) {
                    r.setUnitTypeId(cfg.getUnitType().getId());
                    r.setUnitTypeName(cfg.getUnitType().getName());
                }
                r.setSize(cfg.getSize());
                r.setCreatedAt(cfg.getCreatedAt());
                r.setUpdatedAt(cfg.getUpdatedAt());
                return r;
            }).toList();
            response.setUnitTypeConfigs(cfgs);
        }

        return response;
    }

    private InventoryItemResponse mapInventoryItem(InventoryItem item, String ingredientName, String productName) {
        InventoryItemResponse ir = new InventoryItemResponse();
        ir.setId(item.getId());
        ir.setQuantity(item.getQuantity());
        if (item.getWarehouse() != null) {
            ir.setWarehouseId(item.getWarehouse().getId());
            ir.setWarehouseName(item.getWarehouse().getName());
        }
        ir.setIngredientName(ingredientName);
        ir.setProductName(productName);
        Long cid = item.getCompanyId();
        ir.setCompanyId(cid);
        if (cid != null) {
            try {
                ApiResponse<String> resp = companyClient.getCompanyName(cid);
                if (resp != null && Boolean.TRUE.equals(resp.getSuccess())) {
                    ir.setCompanyName(resp.getData());
                } else if (resp != null && resp.getData() != null) {
                    ir.setCompanyName(resp.getData());
                }
            } catch (Exception ignored) {
            }
        }
        ir.setCreatedAt(item.getCreatedAt());
        ir.setUpdatedAt(item.getUpdatedAt());
        return ir;
    }
}
