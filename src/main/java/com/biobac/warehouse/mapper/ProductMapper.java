package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.response.UnitTypeConfigResponse;
import com.biobac.warehouse.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    @Autowired
    protected CompanyClient companyClient;

    @Autowired
    protected AttributeService attributeService;

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getRecipeItem() != null) {
            response.setRecipeItemName(product.getRecipeItem().getName());
            response.setRecipeItemId(product.getRecipeItem().getId());
        }

        if (product.getUnit() != null) {
            response.setUnitId(product.getUnit().getId());
            response.setUnitName(product.getUnit().getName());
        }

        if (product.getProductGroup() != null) {
            response.setProductGroupId(product.getProductGroup().getId());
            response.setProductGroupName(product.getProductGroup().getName());
        }

        double totalQuantity = product.getInventoryItems()
                .stream()
                .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                .sum();

        List<InventoryItemResponse> inventoryResponses = product.getInventoryItems().stream()
                .map(item -> mapInventoryItem(item, null, product.getName()))
                .toList();
        response.setTotalQuantity(totalQuantity);
        response.setInventoryItems(inventoryResponses);

        if (product.getUnitTypeConfigs() != null) {
            List<UnitTypeConfigResponse> cfgs = product.getUnitTypeConfigs().stream().map(cfg -> {
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
                // Ignore failures from company service
            }
        }
        ir.setCreatedAt(item.getCreatedAt());
        ir.setUpdatedAt(item.getUpdatedAt());
        return ir;
    }
}
