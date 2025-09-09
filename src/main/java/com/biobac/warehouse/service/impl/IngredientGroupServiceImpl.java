package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientGroupMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientGroupResponse;
import com.biobac.warehouse.service.IngredientGroupService;
import com.biobac.warehouse.utils.specifications.IngredientGroupSpecification;
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
public class IngredientGroupServiceImpl implements IngredientGroupService {
    private final IngredientGroupRepository repository;
    private final IngredientGroupMapper mapper;

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
    public List<IngredientGroupResponse> getPagination() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Pair<List<IngredientGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                 Integer page,
                                                                                 Integer size,
                                                                                 String sortBy,
                                                                                 String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<IngredientGroup> spec = IngredientGroupSpecification.buildSpecification(filters);

        Page<IngredientGroup> ingredientGroupPage = repository.findAll(spec, pageable);

        List<IngredientGroupResponse> content = ingredientGroupPage.getContent().stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientGroupPage.getNumber(),
                ingredientGroupPage.getSize(),
                ingredientGroupPage.getTotalElements(),
                ingredientGroupPage.getTotalPages(),
                ingredientGroupPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "ingredientGroupTable"
        );

        return Pair.of(content, metadata);

    }

    @Transactional(readOnly = true)
    @Override
    public IngredientGroupResponse getById(Long id) {
        return mapper.toDto(repository.findById(id).orElseThrow(() -> new NotFoundException("IngredientGroup not found with id: " + id)));
    }

    @Transactional
    @Override
    public IngredientGroupResponse create(IngredientGroupDto dto) {
        IngredientGroup entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public IngredientGroupResponse update(Long id, IngredientGroupDto dto) {
        IngredientGroup existing = repository.findById(id).orElseThrow(() -> new NotFoundException("IngredientGroup not found with id: " + id));
        existing.setName(dto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        IngredientGroup ingredientGroup = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("IngredientGroup not found with id: " + id));
        for (Ingredient ingredient : ingredientGroup.getIngredients()) {
            ingredient.setIngredientGroup(null);
        }

        repository.delete(ingredientGroup);
    }
}