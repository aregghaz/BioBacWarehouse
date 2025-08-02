package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "groupId", source = "group.id")
    })
    IngredientDto toDto(Ingredient entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "group", ignore = true)
    })
    Ingredient toEntity(IngredientDto dto);

    @AfterMapping
    default void setGroup(@MappingTarget Ingredient entity, IngredientDto dto) {
        if (dto.getGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getGroupId());
            entity.setGroup(group);
        }
    }
}
