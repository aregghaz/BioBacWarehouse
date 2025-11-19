package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.*;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ComponentBalanceService {
    Pair<List<ComponentBalanceIngResponse>, PaginationMetadata> getIngPagination(Map<String, FilterCriteria> filters,
                                                                                 Integer page,
                                                                                 Integer size,
                                                                                 String sortBy,
                                                                                 String sortDir);

    Pair<List<ComponentBalanceProdResponse>, PaginationMetadata> getProdPagination(Map<String, FilterCriteria> filters,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   String sortBy,
                                                                                   String sortDir);

    Pair<List<ProductDetailResponse>, PaginationMetadata> getProductDetailsByProductId(Long id, Map<String, FilterCriteria> filters,
                                                                                       Integer page,
                                                                                       Integer size,
                                                                                       String sortBy,
                                                                                       String sortDir);

    Pair<List<IngredientDetailResponse>, PaginationMetadata> getIngredientDetailsByIngredientId(Long id, Map<String, FilterCriteria> filters,
                                                                                                Integer page,
                                                                                                Integer size,
                                                                                                String sortBy,
                                                                                                String sortDir);

    ComponentBalanceQuantityResponse getIngredientBalance(Long ingredientId, Long warehouseId, LocalDateTime date);

    ComponentBalanceQuantityResponse getProductBalance(Long productId, Long warehouseId, LocalDateTime date);

    List<IngredientResponse> getRelatedIngredients(Long warehouseId);

    List<ProductResponse> getRelatedProducts(Long warehouseId);
}
