package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.response.IngredientGroupResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientGroupMapper {

    // Legacy name retained for backwards compatibility
    IngredientGroupResponse toDto(IngredientGroup entity);

    // Preferred naming
    IngredientGroupResponse toResponse(IngredientGroup entity);

    IngredientGroup toEntity(IngredientGroupDto dto);

    IngredientGroupResponse toTableResponse(IngredientGroup ingredientGroup);
}