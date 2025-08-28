package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitDto;
import com.biobac.warehouse.entity.Unit;
import com.biobac.warehouse.entity.UnitType;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.UnitRepository;
import com.biobac.warehouse.repository.UnitTypeRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitCreateRequest;
import com.biobac.warehouse.service.UnitService;
import com.biobac.warehouse.utils.specifications.UnitSpecification;
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
public class UnitServiceImpl implements UnitService {
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public UnitDto getById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        return toDto(unit);
    }

    @Override
    @Transactional
    public UnitDto create(UnitCreateRequest request) {
        UnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(() -> new NotFoundException("Unit Type not found"));
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setUnitType(unitType);
        Unit saved = unitRepository.save(unit);
        return toDto(saved);
    }

    @Override
    @Transactional
    public UnitDto update(Long id, UnitCreateRequest request) {
        Unit existingUnit = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        UnitType unitType = unitTypeRepository.findById(request.getUnitTypeId())
                .orElseThrow(() -> new NotFoundException("Unit Type not found"));
        existingUnit.setName(request.getName());
        existingUnit.setUnitType(unitType);
        Unit saved = unitRepository.save(existingUnit);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        unitRepository.delete(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitDto> getAll() {
        return unitRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<UnitDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Unit> spec = UnitSpecification.buildSpecification(filters);
        Page<Unit> unitPage = unitRepository.findAll(spec, pageable);

        List<UnitDto> content = unitPage.getContent()
                .stream()
                .map(this::toDto)
                .collect(java.util.stream.Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                unitPage.getNumber(),
                unitPage.getSize(),
                unitPage.getTotalElements(),
                unitPage.getTotalPages(),
                unitPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "unitTable"
        );

        return Pair.of(content, metadata);
    }

    private UnitDto toDto(Unit unit) {
        UnitDto dto = new UnitDto();
        dto.setId(unit.getId());
        dto.setName(unit.getName());
        if (unit.getUnitType() != null) {
            dto.setUnitTypeId(unit.getUnitType().getId());
            dto.setUnitTypeName(unit.getUnitType().getName());
        }
        return dto;
    }
}
