package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AssetCategoryRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetCategoryResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface AssetCategoryService {
    AssetCategoryResponse create(AssetCategoryRequest request);

    AssetCategoryResponse update(Long id, AssetCategoryRequest request);

    AssetCategoryResponse getById(Long id);

    List<AssetCategoryResponse> getAll();

    Pair<List<AssetCategoryResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);

    void delete(Long id);
}
