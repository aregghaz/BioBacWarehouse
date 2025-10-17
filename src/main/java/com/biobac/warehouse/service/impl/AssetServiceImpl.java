package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.mapper.AssetMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetResponse;
import com.biobac.warehouse.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
    private final AssetMapper assetMapper;

    @Override
    @Transactional
    public AssetResponse register(AssetRegisterRequest request) {
        return null;
    }

    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRegisterRequest request) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getAll() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AssetResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        return null;
    }
}
