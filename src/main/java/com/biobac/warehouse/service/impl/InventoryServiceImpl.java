package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.ChangeComponentDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ComponentBalanceQuantityResponse;
import com.biobac.warehouse.response.InventoryResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final ProductBalanceRepository productBalanceRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ProductDetailRepository productDetailRepository;



    @Override
    @Transactional
    public void changeProduct(List<ChangeComponentDto> componentDtoList) {

    }

    @Override
    @Transactional
    public void changeIngredient(List<ChangeComponentDto> componentDtoList) {

    }

    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getIngredientBalance(Long ingredientId, Long warehouseId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        IngredientBalance ingredientBalance = ingredientBalanceRepository.findByWarehouseAndIngredient(warehouse, ingredient)
                .orElse(null);

        Double balance = ingredientBalance != null ? ingredientBalance.getBalance() : 0.0;
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }

    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getProductBalance(Long productId, Long warehouseId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        ProductBalance productBalance = productBalanceRepository.findByWarehouseAndProduct(warehouse, product)
                .orElse(null);

        Double balance = productBalance != null ? productBalance.getBalance() : 0.0;
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir, ComponentType type) {
        return null;
    }
}
