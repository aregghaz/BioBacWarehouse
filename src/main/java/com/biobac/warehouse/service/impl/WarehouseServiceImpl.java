
package com.biobac.warehouse.service.impl;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.entity.WarehouseGroup;
import com.biobac.warehouse.entity.WarehouseType;
import com.biobac.warehouse.exception.DeleteException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseGroupRepository;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.repository.WarehouseTypeRepository;
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
    private final WarehouseGroupRepository warehouseGroupRepository;
    private final WarehouseTypeRepository warehouseTypeRepository;

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
        Warehouse warehouse = mapper.toEntity(request);
        if (request.getWarehouseTypeId() != null) {
            WarehouseType type = warehouseTypeRepository.findById(request.getWarehouseTypeId())
                    .orElseThrow(() -> new NotFoundException("Warehouse Type not found"));
            warehouse.setWarehouseType(type);
        }
        WarehouseGroup warehouseGroup = warehouseGroupRepository.findById(request.getWarehouseGroupId())
                .orElseThrow(() -> new NotFoundException("Warehouse group not found"));
        warehouse.setWarehouseGroup(warehouseGroup);
        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            warehouse.setAttributeGroupIds(request.getAttributeGroupIds());
        }
        Warehouse saved = warehouseRepository.save(warehouse);
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeService.createValuesForWarehouse(saved, request.getAttributes());
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    @Override
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse existing = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));
        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        if (request.getWarehouseGroupId() != null) {
            WarehouseGroup warehouseGroup = warehouseGroupRepository.findById(request.getWarehouseGroupId())
                    .orElseThrow(() -> new NotFoundException("Warehouse group not found"));
            existing.setWarehouseGroup(warehouseGroup);
        }
        if (request.getAttributeGroupIds() != null) {
            existing.setAttributeGroupIds(request.getAttributeGroupIds());
        }
        if (request.getWarehouseTypeId() != null) {
            WarehouseType type = warehouseTypeRepository.findById(request.getWarehouseTypeId())
                    .orElseThrow(() -> new NotFoundException("Warehouse Type not found"));
            existing.setWarehouseType(type);
        }
        Warehouse saved = warehouseRepository.save(existing);
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeService.createValuesForWarehouse(saved, request.getAttributes());
        }
        return mapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!warehouseRepository.existsById(id)) {
            throw new NotFoundException("Warehouse not found with id: " + id);
        }
        try {
            attributeService.deleteValuesForWarehouse(id);
            warehouseRepository.deleteById(id);
            warehouseRepository.flush();
        } catch (Exception exception) {
            if (isConstraintViolation(exception)) {
                throw new DeleteException("Warehouse cannot be deleted because it contains inventory items.");
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<WarehouseResponse> getAll() {
        return warehouseRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isConstraintViolation(Throwable ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.springframework.dao.DataIntegrityViolationException) {
                return true;
            }
            if (cause instanceof org.hibernate.exception.ConstraintViolationException) {
                return true;
            }
            String msg = cause.getMessage();
            if (msg != null && msg.toLowerCase().contains("constraint")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
