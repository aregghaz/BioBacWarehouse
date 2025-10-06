package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ReceiveIngredient;
import com.biobac.warehouse.response.ReceiveIngredientResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReceiveIngredientMapper {
    ReceiveIngredientResponse toSingleResponse(ReceiveIngredient item);
}
