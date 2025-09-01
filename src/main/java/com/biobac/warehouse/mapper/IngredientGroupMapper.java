package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.response.IngredientGroupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientGroupMapper {

    IngredientGroupResponse toDto(IngredientGroup entity);

    IngredientGroup toEntity(IngredientGroupDto dto);

    IngredientGroupResponse toTableResponse(IngredientGroup ingredientGroup);
}