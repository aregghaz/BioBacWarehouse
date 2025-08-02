package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.IngredientGroupDto;

import java.util.List;

public interface IngredientGroupService {

    List<IngredientGroupDto> getAll();

    IngredientGroupDto getById(Long id);

    IngredientGroupDto create(IngredientGroupDto dto);

    IngredientGroupDto update(Long id, IngredientGroupDto dto);

    void delete(Long id);
}