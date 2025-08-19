package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientGroupDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.response.IngredientGroupTableResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IngredientGroupMapper {

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name")
    })
    IngredientGroupDto toDto(IngredientGroup entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name")
    })
    IngredientGroup toEntity(IngredientGroupDto dto);

    IngredientGroupTableResponse toTableResponse(IngredientGroup ingredientGroup);
}