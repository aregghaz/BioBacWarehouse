package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientGroupMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientGroupTableResponse;
import com.biobac.warehouse.service.IngredientGroupService;
import com.biobac.warehouse.utils.specifications.IngredientGroupSpecification;
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
public class IngredientGroupServiceImpl implements IngredientGroupService {
    private final IngredientGroupRepository repository;
    private final IngredientGroupMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<IngredientGroupDto> getPagination() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Pair<List<IngredientGroupTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                      Integer page,
                                                                                      Integer size,
                                                                                      String sortBy,
                                                                                      String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<IngredientGroup> spec = IngredientGroupSpecification.buildSpecification(filters);

        Page<IngredientGroup> ingredientGroupPage = repository.findAll(spec, pageable);

        List<IngredientGroupTableResponse> content = ingredientGroupPage.getContent().stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientGroupPage.getNumber(),
                ingredientGroupPage.getSize(),
                ingredientGroupPage.getTotalElements(),
                ingredientGroupPage.getTotalPages(),
                ingredientGroupPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "ingredientTable"
        );

        return Pair.of(content, metadata);

    }

    @Transactional(readOnly = true)
    @Override
    public IngredientGroupDto getById(Long id) {
        return mapper.toDto(repository.findById(id).orElseThrow(() -> new NotFoundException("IngredientGroup not found with id: " + id)));
    }

    @Transactional
    @Override
    public IngredientGroupDto create(IngredientGroupDto dto) {
        IngredientGroup entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional
    @Override
    public IngredientGroupDto update(Long id, IngredientGroupDto dto) {
        IngredientGroup existing = repository.findById(id).orElseThrow(() -> new NotFoundException("IngredientGroup not found with id: " + id));
        existing.setName(dto.getName());
        return mapper.toDto(repository.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("IngredientGroup not found with id: " + id);
        }
        repository.deleteById(id);
    }
}