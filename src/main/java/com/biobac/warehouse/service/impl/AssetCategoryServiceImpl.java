package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.AssetCategory;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AssetCategoryMapper;
import com.biobac.warehouse.repository.AssetCategoryRepository;
import com.biobac.warehouse.request.AssetCategoryRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AssetCategoryResponse;
import com.biobac.warehouse.service.AssetCategoryService;
import com.biobac.warehouse.utils.specifications.AssetCategorySpecification;
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
public class AssetCategoryServiceImpl implements AssetCategoryService {
    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetCategoryMapper assetCategoryMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
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
        return sortBy;
    }

    @Override
    @Transactional
    public AssetCategoryResponse update(Long id, AssetCategoryRequest request) {
        AssetCategory existing = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
        assetCategoryMapper.updateEntityFromRequest(request, existing);

        AssetCategory saved = assetCategoryRepository.save(existing);
        return assetCategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AssetCategoryResponse create(AssetCategoryRequest request) {
        AssetCategory saved = assetCategoryRepository.save(assetCategoryMapper.toEntity(request));
        return assetCategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AssetCategoryResponse getById(Long id) {
        AssetCategory existing = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
        return assetCategoryMapper.toResponse(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> getAll() {
        return assetCategoryRepository.findAll().stream().map(assetCategoryMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AssetCategoryResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<AssetCategory> spec = AssetCategorySpecification.buildSpecification(filters);

        Page<AssetCategory> assetCategoryPage = assetCategoryRepository.findAll(spec, pageable);

        List<AssetCategoryResponse> content = assetCategoryPage.getContent()
                .stream()
                .map(assetCategoryMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                assetCategoryPage.getNumber(),
                assetCategoryPage.getSize(),
                assetCategoryPage.getTotalElements(),
                assetCategoryPage.getTotalPages(),
                assetCategoryPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "assetCategoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public void delete(Long id) {
        AssetCategory existing = assetCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Asset category not found"));
        assetCategoryRepository.delete(existing);
    }
}
