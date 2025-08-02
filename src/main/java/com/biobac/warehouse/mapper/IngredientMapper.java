package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "groupId", source = "group.id"),
        @Mapping(target = "parentIngredientId", source = "parentIngredient.id"),
        @Mapping(target = "childIngredientIds", expression = "java(mapChildIngredientIds(entity))")
    })
    IngredientDto toDto(Ingredient entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "group", ignore = true),
        @Mapping(target = "parentIngredient", ignore = true),
        @Mapping(target = "childIngredients", ignore = true)
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
    
    @AfterMapping
    default void setParentIngredient(@MappingTarget Ingredient entity, IngredientDto dto) {
        if (dto.getParentIngredientId() != null) {
            Ingredient parentIngredient = new Ingredient();
            parentIngredient.setId(dto.getParentIngredientId());
            entity.setParentIngredient(parentIngredient);
        }
    }
    
    default List<Long> mapChildIngredientIds(Ingredient entity) {
        if (entity.getChildIngredients() == null) {
            return new ArrayList<>();
        }
        return entity.getChildIngredients().stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }
}
