package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.response.IngredientDetailResponse;
import com.biobac.warehouse.response.ProductDetailResponse;
import org.springframework.data.util.Pair;

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

    Pair<List<IngredientDetailResponse>, PaginationMetadata> getIngredientDetailsByProductId(Long id, Map<String, FilterCriteria> filters,
                                                                                          Integer page,
                                                                                          Integer size,
                                                                                          String sortBy,
                                                                                          String sortDir);
}
