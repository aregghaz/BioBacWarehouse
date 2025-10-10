package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.WarehouseType;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.WarehouseTypeMapper;
import com.biobac.warehouse.repository.WarehouseTypeRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.WarehouseTypeRequest;
import com.biobac.warehouse.response.WarehouseTypeResponse;
import com.biobac.warehouse.service.WarehouseTypeService;
import com.biobac.warehouse.utils.specifications.WarehouseTypeSpecification;
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
public class WarehouseTypeServiceImpl implements WarehouseTypeService {
    private final WarehouseTypeMapper warehouseTypeMapper;
    private final WarehouseTypeRepository warehouseTypeRepository;

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
    public Pair<List<WarehouseTypeResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<WarehouseType> spec = WarehouseTypeSpecification.buildSpecification(filters);
        Page<WarehouseType> pageResult = warehouseTypeRepository.findAll(spec, pageable);

        List<WarehouseTypeResponse> content = pageResult.getContent().stream()
                .map(warehouseTypeMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "warehouseTypeTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    public WarehouseTypeResponse getById(Long id) {
        WarehouseType warehouseType = warehouseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse Type not found"));
        return warehouseTypeMapper.toResponse(warehouseType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseTypeResponse> getAll() {
        return warehouseTypeRepository
                .findAll()
                .stream()
                .map(warehouseTypeMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public WarehouseTypeResponse create(WarehouseTypeRequest request) {
        WarehouseType entity = new WarehouseType();
        entity.setType(request.getType());
        WarehouseType saved = warehouseTypeRepository.save(entity);
        return warehouseTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WarehouseTypeResponse update(Long id, WarehouseTypeRequest request) {
        WarehouseType existing = warehouseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseType not found with id: " + id));
        existing.setType(request.getType());
        WarehouseType saved = warehouseTypeRepository.save(existing);
        return warehouseTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WarehouseType existing = warehouseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WarehouseType not found with id: " + id));
        warehouseTypeRepository.delete(existing);
    }
}
