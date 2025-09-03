package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitTypeDto;
import com.biobac.warehouse.entity.UnitType;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.UnitTypeRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitTypeCreateRequest;
import com.biobac.warehouse.service.UnitTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
public class UnitTypeServiceImpl implements UnitTypeService {
    private final UnitTypeRepository unitTypeRepository;
    private final com.biobac.warehouse.mapper.UnitTypeMapper unitTypeMapper;

    @Override
    @Transactional
    public UnitTypeDto create(UnitTypeCreateRequest request) {
        UnitType unitType = new UnitType();
        unitType.setName(request.getName());
        UnitType saved = unitTypeRepository.save(unitType);
        return unitTypeMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitTypeDto getById(Long id) {
        UnitType unitType = unitTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit Type not found"));
        return unitTypeMapper.toDto(unitType);
    }

    @Override
    @Transactional
    public UnitTypeDto update(Long id, UnitTypeCreateRequest request) {
        UnitType existing = unitTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit Type not found"));
        existing.setName(request.getName());
        UnitType unitType = unitTypeRepository.save(existing);
        return unitTypeMapper.toDto(unitType);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        UnitType existing = unitTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit Type not found"));
        unitTypeRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypeDto> getAll() {
        return unitTypeRepository.findAll()
                .stream()
                .map(unitTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<UnitTypeDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        Specification<UnitType> spec = com.biobac.warehouse.utils.specifications.UnitTypeSpecification.buildSpecification(filters);
        Page<UnitType> unitTypePage = unitTypeRepository.findAll(spec, pageable);

        List<UnitTypeDto> content = unitTypePage.getContent()
                .stream()
                .map(unitTypeMapper::toDto)
                .collect(java.util.stream.Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                unitTypePage.getNumber(),
                unitTypePage.getSize(),
                unitTypePage.getTotalElements(),
                unitTypePage.getTotalPages(),
                unitTypePage.isLast(),
                filters,
                sortDir,
                sortBy,
                "unitTypeTable"
        );

        return Pair.of(content, metadata);
    }

}
