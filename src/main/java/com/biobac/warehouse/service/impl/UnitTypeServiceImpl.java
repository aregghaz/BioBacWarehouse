package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.UnitTypeDto;
import com.biobac.warehouse.entity.IngredientUnitType;
import com.biobac.warehouse.entity.ProductUnitType;
import com.biobac.warehouse.entity.UnitType;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.UnitTypeMapper;
import com.biobac.warehouse.repository.IngredientUnitTypeRepository;
import com.biobac.warehouse.repository.ProductUnitTypeRepository;
import com.biobac.warehouse.repository.UnitTypeRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.UnitTypeCreateRequest;
import com.biobac.warehouse.service.UnitTypeService;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitTypeServiceImpl implements UnitTypeService {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private final IngredientUnitTypeRepository ingredientUnitTypeRepository;
    private final ProductUnitTypeRepository productUnitTypeRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final UnitTypeMapper unitTypeMapper;

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
        Set<Long> excludeIds = Stream.concat(
                ingredientUnitTypeRepository.findAll().stream()
                        .filter(IngredientUnitType::isBaseType)
                        .map(i -> i.getUnitType().getId()),
                productUnitTypeRepository.findAll().stream()
                        .filter(ProductUnitType::isBaseType)
                        .map(i -> i.getUnitType().getId())
        ).collect(Collectors.toSet());
        return unitTypeRepository.findAll()
                .stream()
                .filter(ut -> !excludeIds.contains(ut.getId()))
                .map(unitTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<UnitTypeDto>, PaginationMetadata> pagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<UnitType> spec = com.biobac.warehouse.utils.specifications.UnitTypeSpecification.buildSpecification(filters);
        Page<UnitType> unitTypePage = unitTypeRepository.findAll(spec, pageable);

        Set<Long> excludeIds = Stream.concat(
                ingredientUnitTypeRepository.findAll().stream()
                        .filter(IngredientUnitType::isBaseType)
                        .map(i -> i.getUnitType().getId()),
                productUnitTypeRepository.findAll().stream()
                        .filter(ProductUnitType::isBaseType)
                        .map(i -> i.getUnitType().getId())
        ).collect(Collectors.toSet());

        List<UnitTypeDto> content = unitTypePage.getContent().stream()
                .filter(unitType -> !excludeIds.contains(unitType.getId()))
                .map(unitTypeMapper::toDto)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                unitTypePage.getNumber(),
                unitTypePage.getSize(),
                unitTypePage.getTotalElements(),
                unitTypePage.getTotalPages(),
                unitTypePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "unitTypeTable"
        );

        return Pair.of(content, metadata);
    }

}
