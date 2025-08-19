
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientTableResponse;
import com.biobac.warehouse.response.WarehouseTableResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public interface IngredientService {


    Pair<List<IngredientTableResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                   Integer page,
                                                                   Integer size,
                                                                   String sortBy,
                                                                   String sortDir);


    IngredientDto getById(Long id);


    IngredientDto create(IngredientDto dto);


    IngredientDto update(Long id, IngredientDto dto);


    void delete(Long id);
}
