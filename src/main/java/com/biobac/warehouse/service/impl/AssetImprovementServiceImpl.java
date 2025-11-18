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
import com.biobac.warehouse.utils.PageUtil;
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
        improvement.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
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
        Pageable pageable = PageUtil.buildPageable(page, size, sortBy, sortDir);

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
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(PageUtil.DEFAULT_SORT_BY),
                "assetImprovementTable"
        );

        return Pair.of(content, metadata);
    }
}
