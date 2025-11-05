package com.biobac.warehouse.controller;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.response.ApiResponse;
import com.biobac.warehouse.response.ComponentBalanceQuantityResponse;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/transfer")
    public ApiResponse<String> transfer(@RequestBody List<TransferComponentDto> request) {
        inventoryService.transfer(request);
        return ResponseUtil.success("Components transfer successfully");
    }

    @PostMapping("/change")
    public ApiResponse<String> change(@RequestBody List<ChangeComponentDto> request) {
        return ResponseUtil.success("Not Implemented");
    }

    @GetMapping("/ingredient-balance")
    public ApiResponse<ComponentBalanceQuantityResponse> getIngredientBalance(@RequestParam Long ingredientId,
                                                                              @RequestParam Long warehouseId) {
        ComponentBalanceQuantityResponse response = inventoryService.getIngredientBalance(ingredientId, warehouseId);
        return ResponseUtil.success("Balance retrieved successfully", response);
    }

    @GetMapping("/product-balance")
    public ApiResponse<ComponentBalanceQuantityResponse> getProductBalance(@RequestParam Long productId,
                                                                           @RequestParam Long warehouseId) {
        ComponentBalanceQuantityResponse response = inventoryService.getProductBalance(productId, warehouseId);
        return ResponseUtil.success("Balance retrieved successfully", response);
    }
}
