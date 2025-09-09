package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseGroupDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.WarehouseGroupMapper;
import com.biobac.warehouse.repository.WarehouseGroupRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.WarehouseGroupResponse;
import com.biobac.warehouse.service.WarehouseGroupService;
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
public class WarehouseGroupServiceImpl implements WarehouseGroupService {

    private final WarehouseGroupRepository repository;
    private final WarehouseGroupMapper mapper;

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
    public List<WarehouseGroupResponse> getPagination() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Pair<List<WarehouseGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<WarehouseGroup> spec = null; // No filters implemented for now

        Page<WarehouseGroup> groupPage = (spec == null)
                ? repository.findAll(pageable)
                : repository.findAll(spec, pageable);

        List<WarehouseGroupResponse> content = groupPage.getContent().stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                groupPage.getNumber(),
                groupPage.getSize(),
                groupPage.getTotalElements(),
                groupPage.getTotalPages(),
                groupPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "warehouseGroupTable"
        );

        return Pair.of(content, metadata);

    }

    @Transactional(readOnly = true)
    @Override
    public WarehouseGroupResponse getById(Long id) {
        WarehouseGroup entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseGroup not found with id: " + id));
        return mapper.toDto(entity);
    }

    @Transactional
    @Override
    public WarehouseGroupResponse create(WarehouseGroupDto dto) {
        WarehouseGroup entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public WarehouseGroupResponse update(Long id, WarehouseGroupDto dto) {
        WarehouseGroup existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseGroup not found with id: " + id));
        existing.setName(dto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Override
    public void delete(Long id) {
        WarehouseGroup warehouseGroup = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseGroup not found with id: " + id));
        for (Warehouse warehouse : warehouseGroup.getWarehouses()){
            warehouse.setWarehouseGroup(null);
        }
        repository.delete(warehouseGroup);
    }
}
