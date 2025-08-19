package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientGroupTableResponse;
import com.biobac.warehouse.response.IngredientTableResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface IngredientGroupService {

    Pair<List<IngredientGroupTableResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                        Integer page,
                                                                        Integer size,
                                                                        String sortBy,
                                                                        String sortDir);

    IngredientGroupDto getById(Long id);

    IngredientGroupDto create(IngredientGroupDto dto);

    IngredientGroupDto update(Long id, IngredientGroupDto dto);

    void delete(Long id);
}