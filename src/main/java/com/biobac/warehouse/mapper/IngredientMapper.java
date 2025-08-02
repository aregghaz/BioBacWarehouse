package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mapping(source = "group.id", target = "groupId")
    IngredientDto toDto(Ingredient entity);

    @Mapping(target = "group.id", source = "groupId")
    Ingredient toEntity(IngredientDto dto);
}
