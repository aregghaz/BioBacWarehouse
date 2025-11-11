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
    List<IngredientGroupResponse> getPagination();

    Pair<List<IngredientGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                          Integer page,
                                                                          Integer size,
                                                                          String sortBy,
                                                                          String sortDir);

    IngredientGroupResponse getById(Long id);

    IngredientGroupResponse create(IngredientGroupDto dto);

    IngredientGroupResponse update(Long id, IngredientGroupDto dto);

    void delete(Long id);
}