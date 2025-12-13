package com.biobac.warehouse.client;

import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "permission-service", url = "${services.user-url}")
public interface UserClient {
    @GetMapping("/roles/warehouse-groups/{userId}")
    ApiResponse<List<Long>> getWarehouseGroupIds(@PathVariable Long userId);

    @GetMapping("/roles/ingredient-groups/{userId}")
    ApiResponse<List<Long>> getIngredientGroupIds(@PathVariable Long userId);

    @GetMapping("/roles/product-groups/{userId}")
    ApiResponse<List<Long>> getProductGroupIds(@PathVariable Long userId);

    @GetMapping("/users/admin/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable Long userId);
}
