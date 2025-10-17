package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface AssetService {
    AssetResponse register(AssetRegisterRequest request);

    AssetResponse update(Long id, AssetRegisterRequest request);

    AssetResponse getById(Long id);

    List<AssetResponse> getAll();

    Pair<List<AssetResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);
}
