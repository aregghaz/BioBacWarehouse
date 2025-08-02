package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    IngredientDto toDto(Ingredient entity);
    Ingredient toEntity(IngredientDto dto);
}
