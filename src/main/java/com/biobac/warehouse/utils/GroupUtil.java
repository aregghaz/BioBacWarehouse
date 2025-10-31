package com.biobac.warehouse.utils;

import com.biobac.warehouse.client.UserClient;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.exception.ExternalServiceException;
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

    public List<Long> getAccessibleProductGroupIds() {
        return getAccessibleGroupIds(userClient::getProductGroupIds, "product");
    }

    public List<Long> getAccessibleIngredientGroupIds() {
        return getAccessibleGroupIds(userClient::getIngredientGroupIds, "ingredient");
    }

    public List<Long> getAccessibleWarehouseGroupIds() {
        List<Long> groupIds;
        if (accessToAllGroups()) {
            groupIds = warehouseGroupRepository.findAll().stream().map(WarehouseGroup::getId).toList();
        } else {
            groupIds = getAccessibleWarehouseGroupIds();
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
