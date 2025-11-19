package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitDto;
import com.biobac.warehouse.dto.UnitTypeDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final com.biobac.warehouse.mapper.UnitMapper unitMapper;

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
    public UnitDto getById(Long id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        return unitMapper.toDto(unit);
    }

    @Override
    @Transactional
    public UnitDto create(UnitCreateRequest request) {
        List<UnitType> unitTypes = unitTypeRepository.findAllById(request.getUnitTypeIds());
        if (unitTypes.isEmpty()) {
            throw new NotFoundException("Unit Type(s) not found");
        }
        Unit unit = new Unit();
        unit.setName(request.getName());
        unit.setUnitTypes(new HashSet<>(unitTypes));
        Unit saved = unitRepository.save(unit);
        return unitMapper.toDto(saved);
    }

    @Override
    @Transactional
    public UnitDto update(Long id, UnitCreateRequest request) {
        Unit existingUnit = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        List<UnitType> unitTypes = unitTypeRepository.findAllById(request.getUnitTypeIds());
        if (unitTypes.isEmpty()) {
            throw new NotFoundException("Unit Type(s) not found");
        }
        existingUnit.setName(request.getName());
        existingUnit.setUnitTypes(new HashSet<>(unitTypes));
        Unit saved = unitRepository.save(existingUnit);
        return unitMapper.toDto(saved);
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
                .map(unitMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<UnitDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Unit> spec = UnitSpecification.buildSpecification(filters);
        Page<Unit> unitPage = unitRepository.findAll(spec, pageable);

        List<UnitDto> content = unitPage.getContent()
                .stream()
                .map(unitMapper::toDto)
                .collect(java.util.stream.Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                unitPage.getNumber(),
                unitPage.getSize(),
                unitPage.getTotalElements(),
                unitPage.getTotalPages(),
                unitPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "unitTable"
        );

        return Pair.of(content, metadata);
    }

}
