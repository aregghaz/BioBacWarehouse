package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientGroupTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IngredientGroupService {

    @Transactional(readOnly = true)
    List<IngredientGroupDto> getPagination();

    @Transactional(readOnly = true)
    Pair<List<IngredientGroupTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                               Integer page,
                                                                               Integer size,
                                                                               String sortBy,
                                                                               String sortDir);

    @Transactional(readOnly = true)
    IngredientGroupDto getById(Long id);

    @Transactional
    IngredientGroupDto create(IngredientGroupDto dto);

    @Transactional
    IngredientGroupDto update(Long id, IngredientGroupDto dto);

    @Transactional
    void delete(Long id);
}