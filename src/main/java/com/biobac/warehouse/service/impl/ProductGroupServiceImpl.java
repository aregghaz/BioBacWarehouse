package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductGroupDto;
import com.biobac.warehouse.entity.ProductGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ProductGroupMapper;
import com.biobac.warehouse.repository.ProductGroupRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ProductGroupResponse;
import com.biobac.warehouse.service.ProductGroupService;
import com.biobac.warehouse.utils.specifications.ProductGroupSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGroupServiceImpl implements ProductGroupService {
    private final ProductGroupRepository repository;
    private final ProductGroupMapper mapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size <= 0 ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        String sd = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir;
        Sort sort = sd.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        if (safeSize > 1000) {
            log.warn("Requested page size {} is too large, capping to 1000", safeSize);
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductGroupResponse> getPagination() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Pair<List<ProductGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                              Integer page,
                                                                              Integer size,
                                                                              String sortBy,
                                                                              String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<ProductGroup> spec = ProductGroupSpecification.buildSpecification(filters);

        Page<ProductGroup> productGroupPage = repository.findAll(spec, pageable);

        List<ProductGroupResponse> content = productGroupPage.getContent().stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                productGroupPage.getNumber(),
                productGroupPage.getSize(),
                productGroupPage.getTotalElements(),
                productGroupPage.getTotalPages(),
                productGroupPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productGroupTable"
        );

        return Pair.of(content, metadata);

    }

    @Transactional(readOnly = true)
    @Override
    public ProductGroupResponse getById(Long id) {
        return mapper.toDto(repository.findById(id).orElseThrow(() -> new NotFoundException("ProductGroup not found with id: " + id)));
    }

    @Transactional
    @Override
    public ProductGroupResponse create(ProductGroupDto dto) {
        ProductGroup entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public ProductGroupResponse update(Long id, ProductGroupDto dto) {
        ProductGroup existing = repository.findById(id).orElseThrow(() -> new NotFoundException("ProductGroup not found with id: " + id));
        existing.setName(dto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("ProductGroup not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
