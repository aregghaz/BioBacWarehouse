package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.UnitTypeConfigResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {

    @Autowired
    protected CompanyClient companyClient;

    public IngredientResponse toResponse(Ingredient ingredient) {
        if (ingredient == null) return null;
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setActive(ingredient.isActive());
        if (ingredient.getIngredientGroup() != null) {
            response.setIngredientGroupId(ingredient.getIngredientGroup().getId());
            response.setIngredientGroupName(ingredient.getIngredientGroup().getName());
        }
        response.setCreatedAt(ingredient.getCreatedAt());
        response.setUpdatedAt(ingredient.getUpdatedAt());

        if (ingredient.getRecipeItem() != null) {
            response.setRecipeItemId(ingredient.getRecipeItem().getId());
            response.setRecipeItemName(ingredient.getRecipeItem().getName());
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
                // If company service is unavailable, leave companyName null
            }
        }
        ir.setCreatedAt(item.getCreatedAt());
        ir.setUpdatedAt(item.getUpdatedAt());
        return ir;
    }
}
