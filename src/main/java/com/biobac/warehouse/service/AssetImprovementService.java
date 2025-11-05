package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetImprovementResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface AssetImprovementService {
    AssetImprovementResponse addImprovement(Long assetId, AddImprovementRequest request);

    Pair<List<AssetImprovementResponse>, PaginationMetadata> getPaginationByAssetId(Long assetId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir);
}
