package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientGroupResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IngredientGroupService {

    @Transactional(readOnly = true)
    List<IngredientGroupResponse> getPagination();

    @Transactional(readOnly = true)
    Pair<List<IngredientGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                          Integer page,
                                                                          Integer size,
                                                                          String sortBy,
                                                                          String sortDir);

    @Transactional(readOnly = true)
    IngredientGroupResponse getById(Long id);

    @Transactional
    IngredientGroupResponse create(IngredientGroupDto dto);

    @Transactional
    IngredientGroupResponse update(Long id, IngredientGroupDto dto);

    @Transactional
    void delete(Long id);
}