package com.biobac.warehouse.service;

import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.response.AssetImprovementResponse;

import java.util.List;

public interface AssetImprovementService {
    AssetImprovementResponse addImprovement(Long assetId, AddImprovementRequest request);

    List<AssetImprovementResponse> getByAssetId(Long assetId);
}
