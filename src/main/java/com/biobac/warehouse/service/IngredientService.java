
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientTableResponse;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


public interface IngredientService {

    @Transactional(readOnly = true)
    List<IngredientDto> getAll();

    @Transactional(readOnly = true)
    Pair<List<IngredientTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                          Integer page,
                                                                          Integer size,
                                                                          String sortBy,
                                                                          String sortDir);


    @Transactional(readOnly = true)
    IngredientDto getById(Long id);


    @Transactional
    IngredientDto create(IngredientDto dto);


    @Transactional
    IngredientDto update(Long id, IngredientDto dto);

    @Transactional
    void delete(Long id);
}
