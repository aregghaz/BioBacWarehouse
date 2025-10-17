package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.Asset;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetStatusRepository assetStatusRepository;
    private final AssetImprovementRepository assetImprovementRepository;
    private final AssetRepository assetRepository;
    private final DepartmentRepository departmentRepository;
    private final DepreciationMethodRepository depreciationMethodRepository;
    private final DepreciationRecordRepository depreciationRecordRepository;

    @Override
    @Transactional
    public Asset register(AssetRegisterRequest request) {
        return null;
    }
}
