package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.TransferComponentDto;
import com.biobac.warehouse.response.ComponentBalanceQuantityResponse;

import java.util.List;

public interface InventoryService {
    void transfer(List<TransferComponentDto> componentDtoList);

    void change(List<ChangeComponentDto> componentDtoList);

    ComponentBalanceQuantityResponse getIngredientBalance(Long ingredientId, Long warehouseId);

    ComponentBalanceQuantityResponse getProductBalance(Long productId, Long warehouseId);
}
