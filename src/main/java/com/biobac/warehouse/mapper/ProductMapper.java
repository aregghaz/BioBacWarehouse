package com.biobac.warehouse.mapper;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.entity.AttributeTargetType;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ProductMapper {

    @Autowired
    protected CompanyClient companyClient;

    @Autowired
    protected AttributeClient attributeClient;

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setExpiration(product.getExpiration());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());
        response.setMinimalBalance(product.getMinimalBalance());
        response.setDefaultWarehouseName(product.getDefaultWarehouse().getName());
        response.setDefaultWarehouseId(product.getDefaultWarehouse().getId());
        response.setAttributeGroupIds(product.getAttributeGroupIds());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getExtraComponents() != null && !product.getExtraComponents().isEmpty()) {
            List<ExtraComponentsResponse> comps = product.getExtraComponents().stream()
                    .map(comp -> {
                        ExtraComponentsResponse resp = new ExtraComponentsResponse();
                        resp.setIngredientId(comp.getIngredient() != null ? comp.getIngredient().getId() : null);
                        resp.setProductId(comp.getChildProduct() != null ? comp.getChildProduct().getId() : null);
                        resp.setName(comp.getIngredient() != null
                                ? comp.getIngredient().getName()
                                : Objects.requireNonNull(comp.getChildProduct()).getName());

                        double baseQuantity = comp.getQuantity();
                        List<UnitTypeCalculatedResponse> unitTypes;

                        if (comp.getChildProduct() != null) {
                            unitTypes = comp.getChildProduct().getUnitTypeConfigs().stream()
                                    .map(utc -> {
                                        UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
                                        calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
                                        calculatedResponse.setUnitTypeId(utc.getId());
                                        calculatedResponse.setBaseUnit(utc.isBaseType());
                                        if (utc.isBaseType()) {
                                            calculatedResponse.setSize(baseQuantity);
                                        } else {
                                            calculatedResponse.setSize(Math.ceil(baseQuantity / utc.getSize()));
                                        }
                                        return calculatedResponse;
                                    })
                                    .toList();
                        } else {
                            unitTypes = comp.getIngredient().getUnitTypeConfigs().stream()
                                    .map(utc -> {
                                        UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
                                        calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
                                        calculatedResponse.setUnitTypeId(utc.getId());
                                        calculatedResponse.setBaseUnit(utc.isBaseType());
                                        if (utc.isBaseType()) {
                                            calculatedResponse.setSize(baseQuantity);
                                        } else {
                                            calculatedResponse.setSize(Math.ceil(baseQuantity / utc.getSize()));
                                        }
                                        return calculatedResponse;
                                    })
                                    .toList();
                        }

                        resp.setUnitTypeConfigs(unitTypes);
                        return resp;
                    })
                    .toList();

            response.setExtraComponents(comps);
        }

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
                r.setBaseUnit(cfg.isBaseType());
                r.setSize(cfg.getSize());
                r.setCreatedAt(cfg.getCreatedAt());
                r.setUpdatedAt(cfg.getUpdatedAt());
                return r;
            }).toList();
            response.setUnitTypeConfigs(cfgs);
        }

        try {
            if (product.getId() != null) {
                ApiResponse<List<AttributeResponse>> apiResponse = attributeClient.getValues(product.getId(), AttributeTargetType.PRODUCT.name());
                response.setAttributes(apiResponse.getData());
            }
        } catch (Exception ignored) {
        }

        return response;
    }

    private List<UnitTypeCalculatedResponse> calculateExtraComponentUnits(Product product) {
        if (product.getExtraComponents() == null) {
            return List.of();
        }

        return product.getExtraComponents().stream()
                .flatMap(comp -> {
                    double baseQuantity = comp.getQuantity();

                    if (comp.getChildProduct() != null) {
                        return comp.getChildProduct().getUnitTypeConfigs().stream()
                                .map(utc -> {
                                    UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
                                    calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
                                    calculatedResponse.setUnitTypeId(utc.getId());
                                    calculatedResponse.setBaseUnit(utc.isBaseType());
                                    if (utc.isBaseType()) {
                                        calculatedResponse.setSize(baseQuantity);
                                    } else {
                                        calculatedResponse.setSize(Math.ceil(baseQuantity / utc.getSize()));
                                    }
                                    return calculatedResponse;
                                });
                    } else {
                        return comp.getIngredient().getUnitTypeConfigs().stream()
                                .map(utc -> {
                                    UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
                                    calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
                                    calculatedResponse.setUnitTypeId(utc.getId());
                                    calculatedResponse.setBaseUnit(utc.isBaseType());
                                    if (utc.isBaseType()) {
                                        calculatedResponse.setSize(baseQuantity);
                                    } else {
                                        calculatedResponse.setSize(Math.ceil(baseQuantity / utc.getSize()));
                                    }
                                    return calculatedResponse;
                                });
                    }
                })
                .toList();
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
