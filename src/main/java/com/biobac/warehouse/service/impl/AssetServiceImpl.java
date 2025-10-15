package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
