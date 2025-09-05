
package com.biobac.warehouse.service.impl;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseRequest;
import com.biobac.warehouse.response.WarehouseResponse;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.specifications.WarehouseSpecification;
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
public class WarehouseServiceImpl implements WarehouseService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;
    private final AttributeService attributeService;

    @Transactional(readOnly = true)
    @Override
    public Pair<List<WarehouseResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Warehouse> spec = WarehouseSpecification.buildSpecification(filters);

        Page<Warehouse> warehousePage = warehouseRepository.findAll(spec, pageable);

        List<WarehouseResponse> content = warehousePage.getContent()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                warehousePage.getNumber(),
                warehousePage.getSize(),
                warehousePage.getTotalElements(),
                warehousePage.getTotalPages(),
                warehousePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "warehouseTable"
        );

        return Pair.of(content, metadata);
    }

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

    @Transactional(readOnly = true)
    @Override
    public WarehouseResponse getById(Long id) {
        Warehouse entity = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));
        return mapper.toResponse(entity);
    }

    @Transactional
    @Override
    public WarehouseResponse create(WarehouseRequest request) {
        Warehouse saved = warehouseRepository.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }

    @Transactional
    @Override
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse existing = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));
        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        existing.setType(request.getType());
        Warehouse saved = warehouseRepository.save(existing);
        return mapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new NotFoundException("Warehouse not found with id: " + id);
        }
        warehouseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}
