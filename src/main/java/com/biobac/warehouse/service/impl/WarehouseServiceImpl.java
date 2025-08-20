
package com.biobac.warehouse.service.impl;


import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.WarehouseDto;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.WarehouseMapper;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.WarehouseTableResponse;
import com.biobac.warehouse.service.WarehouseService;
import com.biobac.warehouse.utils.specifications.WarehouseSpecification;
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
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public Pair<List<WarehouseTableResponse>, PaginationMetadata> getPagination(
            Map<String, FilterCriteria> filters,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Warehouse> spec = WarehouseSpecification.buildSpecification(filters);

        Page<Warehouse> warehousePage = warehouseRepository.findAll(spec, pageable);

        List<WarehouseTableResponse> content = warehousePage.getContent()
                .stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                warehousePage.getNumber(),
                warehousePage.getSize(),
                warehousePage.getTotalElements(),
                warehousePage.getTotalPages(),
                warehousePage.isLast(),
                filters,
                sortDir,
                sortBy,
                "warehouseTable"
        );

        return Pair.of(content, metadata);
    }

    @Transactional(readOnly = true)
    @Override
    public WarehouseDto getById(Long id) {
        return mapper.toDto(warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id)));
    }

    @Transactional
    @Override
    public WarehouseDto create(WarehouseDto dto) {
        return mapper.toDto(warehouseRepository.save(mapper.toEntity(dto)));
    }

    @Transactional
    @Override
    public WarehouseDto update(Long id, WarehouseDto dto) {
        Warehouse existing = warehouseRepository.findById(id).orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + id));
        existing.setName(dto.getName());
        existing.setLocation(dto.getLocation());
        existing.setType(dto.getType());
        return mapper.toDto(warehouseRepository.save(existing));
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
    public List<WarehouseDto> getAll() {
        return warehouseRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
