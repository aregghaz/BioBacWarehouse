package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.DuplicateException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AssetMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.AssetRegisterRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetResponse;
import com.biobac.warehouse.service.AssetService;
import com.biobac.warehouse.utils.specifications.AssetSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetRepository assetRepository;
    private final DepartmentRepository departmentRepository;
    private final DepreciationMethodRepository depreciationMethodRepository;
    private final AssetMapper assetMapper;
    private final WarehouseRepository warehouseRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "timestamp";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        String mappedSortBy = mapSortField(safeSortBy);

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(mappedSortBy).ascending()
                : Sort.by(mappedSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "categoryName" -> "category.name";
            case "depreciationMethodName" -> "depreciationMethod.name";
            case "departmentName" -> "department.name";
            case "warehouseName" -> "warehouse.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional
    public AssetResponse create(AssetRegisterRequest request) {
        assetRepository.findByCode(request.getCode())
                .ifPresent(a -> {
                    throw new DuplicateException("Asset with code '" + request.getCode() + "' already exists");
                });

        DepreciationMethod depreciationMethod = depreciationMethodRepository.findById(request.getDepreciationMethodId())
                .orElseThrow(() -> new NotFoundException("Depreciation Method not found"));
        AssetCategory category = assetCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        Asset asset = assetMapper.toEntity(request);
        asset.setCategory(category);
        asset.setDepreciationMethod(depreciationMethod);
        asset.setDepartment(department);
        asset.setWarehouse(warehouse);
        asset.setCurrentCost(request.getOriginalCost());

        Asset saved = assetRepository.save(asset);
        return assetMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AssetResponse update(Long id, AssetRegisterRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found with id: " + id));

        if (!asset.getCode().equals(request.getCode())) {
            assetRepository.findByCode(request.getCode())
                    .ifPresent(a -> {
                        throw new DuplicateException("Asset with code '" + request.getCode() + "' already exists");
                    });
            asset.setCode(request.getCode());
        }

        DepreciationMethod depreciationMethod = depreciationMethodRepository.findById(request.getDepreciationMethodId())
                .orElseThrow(() -> new NotFoundException("Depreciation Method not found"));
        AssetCategory category = assetCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        assetMapper.updateEntityFromRequest(request, asset);

        asset.setCategory(category);
        asset.setDepreciationMethod(depreciationMethod);
        asset.setDepartment(department);
        asset.setWarehouse(warehouse);
        asset.setCurrentCost(request.getOriginalCost());

        Asset updated = assetRepository.save(asset);
        return assetMapper.toResponse(updated);
    }


    @Override
    @Transactional(readOnly = true)
    public AssetResponse getById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        return assetMapper.toResponse(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getAll() {
        return assetRepository.findAll().stream().map(assetMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        assetRepository.delete(asset);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AssetResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<Asset> spec = AssetSpecification.buildSpecification(filters);

        Page<Asset> assetPage = assetRepository.findAll(spec, pageable);

        List<AssetResponse> content = assetPage.getContent()
                .stream()
                .map(assetMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                assetPage.getNumber(),
                assetPage.getSize(),
                assetPage.getTotalElements(),
                assetPage.getTotalPages(),
                assetPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "assetTable"
        );

        return Pair.of(content, metadata);
    }
}
