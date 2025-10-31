package com.biobac.warehouse.utils;

import com.biobac.warehouse.client.UserClient;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.entity.ProductGroup;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.exception.ExternalServiceException;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.ProductGroupRepository;
import com.biobac.warehouse.repository.WarehouseGroupRepository;
import com.biobac.warehouse.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class GroupUtil {

    private final UserClient userClient;
    private final SecurityUtil securityUtil;
    private final WarehouseGroupRepository warehouseGroupRepository;
    private final ProductGroupRepository productGroupRepository;
    private final IngredientGroupRepository ingredientGroupRepository;

    public List<Long> getAccessibleProductGroupIds() {
        List<Long> groupIds;
        if (accessToAllGroups()) {
            groupIds = productGroupRepository.findAll().stream().map(ProductGroup::getId).toList();
        } else {
            groupIds = getAccessibleGroupIds(userClient::getProductGroupIds, "product");
        }
        return groupIds;
    }

    public List<Long> getAccessibleIngredientGroupIds() {
        List<Long> groupIds;
        if (accessToAllGroups()) {
            groupIds = ingredientGroupRepository.findAll().stream().map(IngredientGroup::getId).toList();
        } else {
            groupIds = getAccessibleGroupIds(userClient::getProductGroupIds, "ingredient");
        }
        return groupIds;
    }

    public List<Long> getAccessibleWarehouseGroupIds() {
        List<Long> groupIds;
        if (accessToAllGroups()) {
            groupIds = warehouseGroupRepository.findAll().stream().map(WarehouseGroup::getId).toList();
        } else {
            groupIds = getAccessibleGroupIds(userClient::getWarehouseGroupIds, "warehouse");
        }
        return groupIds;
    }

    private boolean accessToAllGroups() {
        return securityUtil.hasPermission("ALL_GROUP_ACCESS");
    }

    private List<Long> getAccessibleGroupIds(Function<Long, ApiResponse<List<Long>>> fetcher, String type) {
        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            throw new ExternalServiceException("User ID not found in security context");
        }

        ApiResponse<List<Long>> resp = fetcher.apply(userId);

        if (resp == null || !resp.getSuccess()) {
            throw new ExternalServiceException(
                    resp != null ? resp.getMessage() :
                            "Failed to fetch " + type + " group IDs"
            );
        }

        return resp.getData() != null ? resp.getData() : List.of();
    }
}
