package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Asset;
import com.biobac.warehouse.entity.AssetAction;
import com.biobac.warehouse.entity.AssetImprovement;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AssetImprovementMapper;
import com.biobac.warehouse.repository.AssetActionRepository;
import com.biobac.warehouse.repository.AssetImprovementRepository;
import com.biobac.warehouse.repository.AssetRepository;
import com.biobac.warehouse.request.AddImprovementRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetImprovementResponse;
import com.biobac.warehouse.service.AssetImprovementService;
import com.biobac.warehouse.utils.specifications.AssetImprovementSpecification;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetImprovementServiceImpl implements AssetImprovementService {
    private final AssetImprovementRepository assetImprovementRepository;
    private final AssetRepository assetRepository;
    private final AssetImprovementMapper assetImprovementMapper;
    private final AssetActionRepository assetActionRepository;

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
    public AssetImprovementResponse addImprovement(Long assetId, AddImprovementRequest request) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        AssetAction action = assetActionRepository.findById(request.getActionId())
                .orElseThrow(() -> new NotFoundException("Action ot found"));

        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Improvement amount is required");
        }
        BigDecimal changedAmount = request.getAmount().subtract(asset.getCurrentCost());

        AssetImprovement improvement = new AssetImprovement();
        improvement.setAsset(asset);
        improvement.setAction(action);
        improvement.setAmount(changedAmount);
        improvement.setExtendLife(request.getMonthsExtended() > asset.getUsefulLifeMonths());
        improvement.setDate(request.getDate()!= null ? request.getDate() : LocalDateTime.now());
        improvement.setComment(request.getComment());
        improvement.setMonthsExtended(request.getMonthsExtended() - asset.getUsefulLifeMonths());
        asset.setCurrentCost(request.getAmount());
        asset.setUsefulLifeMonths(request.getMonthsExtended());
        asset.recalcResidual();

        assetRepository.save(asset);
        AssetImprovement saved = assetImprovementRepository.save(improvement);

        return assetImprovementMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AssetImprovementResponse>, PaginationMetadata> getPaginationByAssetId(Long assetId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<AssetImprovement> spec = AssetImprovementSpecification.buildSpecification(filters)
                .and((root, query, cb) -> cb.equal(root.join("asset", JoinType.LEFT).get("id"), assetId));

        Page<AssetImprovement> assetPage = assetImprovementRepository.findAll(spec, pageable);

        List<AssetImprovementResponse> content = assetPage.getContent()
                .stream()
                .map(assetImprovementMapper::toResponse)
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
                "assetImprovementTable"
        );

        return Pair.of(content, metadata);
    }
}
