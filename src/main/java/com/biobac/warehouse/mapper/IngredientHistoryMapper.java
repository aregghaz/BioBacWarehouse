package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.entity.IngredientHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientHistoryMapper {
    
    IngredientHistoryMapper INSTANCE = Mappers.getMapper(IngredientHistoryMapper.class);
    
    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    IngredientHistoryDto toDto(IngredientHistory entity);
    
    List<IngredientHistoryDto> toDtoList(List<IngredientHistory> entities);
    
    @Mapping(target = "ingredient", ignore = true)
    IngredientHistory toEntity(IngredientHistoryDto dto);
}