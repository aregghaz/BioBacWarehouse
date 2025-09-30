package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.ProductResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface ProductService extends UnitTypeCalculator {
    ProductResponse create(ProductCreateRequest request);

    ProductResponse getById(Long id);

    List<ProductResponse> getAll();

    Pair<List<ProductResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    ProductResponse update(Long id, ProductUpdateRequest request);

    void delete(Long id);

    List<ProductResponse> getAllExcludeRecipeIngredient(Long recipeItemId);
}
