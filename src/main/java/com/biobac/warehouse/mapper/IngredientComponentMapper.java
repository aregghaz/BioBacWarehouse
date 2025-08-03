package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientComponentDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientComponent;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface IngredientComponentMapper {

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "childIngredientId", source = "childIngredient.id"),
        @Mapping(target = "childIngredientName", source = "childIngredient.name"),
        @Mapping(target = "quantity", source = "quantity")
    })
    IngredientComponentDto toDto(IngredientComponent entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "childIngredient", ignore = true),
        @Mapping(target = "parentIngredient", ignore = true),
        @Mapping(target = "quantity", source = "quantity")
    })
    IngredientComponent toEntity(IngredientComponentDto dto);

    @AfterMapping
    default void setChildIngredient(@MappingTarget IngredientComponent entity, IngredientComponentDto dto) {
        if (dto.getChildIngredientId() != null) {
            Ingredient childIngredient = new Ingredient();
            childIngredient.setId(dto.getChildIngredientId());
            entity.setChildIngredient(childIngredient);
        }
    }
}