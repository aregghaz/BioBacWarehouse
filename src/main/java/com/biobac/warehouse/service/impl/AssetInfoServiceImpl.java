package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.AssetAction;
import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.entity.AssetStatus;
import com.biobac.warehouse.entity.DepreciationMethod;
import com.biobac.warehouse.repository.AssetActionRepository;
import com.biobac.warehouse.repository.AssetCategoryRepository;
import com.biobac.warehouse.repository.AssetStatusRepository;
import com.biobac.warehouse.repository.DepreciationMethodRepository;
import com.biobac.warehouse.service.AssetInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetInfoServiceImpl implements AssetInfoService {
    private final AssetStatusRepository assetStatusRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final DepreciationMethodRepository depreciationMethodRepository;
    private final AssetActionRepository assetActionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssetCategory> getAssetCategories() {
        return assetCategoryRepository.findAll().stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetStatus> getAssetStatuses() {
        return assetStatusRepository.findAll().stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepreciationMethod> getAssetDepreciationMethods() {
        return depreciationMethodRepository.findAll().stream().toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetAction> getAssetActions() {
        return assetActionRepository.findAll().stream().toList();
    }
}
