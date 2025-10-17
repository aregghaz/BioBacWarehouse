package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.repository.AssetImprovementRepository;
import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.response.AssetImprovementResponse;
import com.biobac.warehouse.service.AssetImprovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetImprovementServiceImpl implements AssetImprovementService {
    private final AssetImprovementRepository assetImprovementRepository;

    @Override
    @Transactional
    public AssetImprovementResponse addImprovement(Long assetId, AddImprovementRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetImprovementResponse> getByAssetId(Long assetId) {
        return List.of();
    }
}
