package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.ComponentBalance;
import com.biobac.warehouse.mapper.ComponentBalanceMapper;
import com.biobac.warehouse.repository.ComponentBalanceRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.service.ComponentBalanceService;
import com.biobac.warehouse.utils.specifications.ComponentBalanceSpecification;
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
public class ComponentBalanceServiceImpl implements ComponentBalanceService {
    private final ComponentBalanceRepository componentBalanceRepository;
    private final ComponentBalanceMapper componentBalanceMapper;

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
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ComponentBalanceIngResponse>, PaginationMetadata> getIngPagination(Map<String, FilterCriteria> filters,
                                                                                        Integer page,
                                                                                        Integer size,
                                                                                        String sortBy,
                                                                                        String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ComponentBalance> spec = ComponentBalanceSpecification.buildSpecification(filters, "ingredient")
                .and((root, query, cb) -> cb.isNotNull(root.get("ingredient")));

        Page<ComponentBalance> componentBalancePage = componentBalanceRepository.findAll(spec, pageable);

        List<ComponentBalanceIngResponse> content = componentBalancePage.getContent()
                .stream()
                .map(componentBalanceMapper::toIngResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                componentBalancePage.getNumber(),
                componentBalancePage.getSize(),
                componentBalancePage.getTotalElements(),
                componentBalancePage.getTotalPages(),
                componentBalancePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "balanceTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ComponentBalanceProdResponse>, PaginationMetadata> getProdPagination(Map<String, FilterCriteria> filters,
                                                                                          Integer page,
                                                                                          Integer size,
                                                                                          String sortBy,
                                                                                          String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ComponentBalance> spec = ComponentBalanceSpecification.buildSpecification(filters, "product")
                .and((root, query, cb) -> cb.isNotNull(root.get("product")));
        Page<ComponentBalance> componentBalancePage = componentBalanceRepository.findAll(spec, pageable);

        List<ComponentBalanceProdResponse> content = componentBalancePage.getContent()
                .stream()
                .map(componentBalanceMapper::toProdResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                componentBalancePage.getNumber(),
                componentBalancePage.getSize(),
                componentBalancePage.getTotalElements(),
                componentBalancePage.getTotalPages(),
                componentBalancePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "balanceTable"
        );

        return Pair.of(content, metadata);
    }
}
