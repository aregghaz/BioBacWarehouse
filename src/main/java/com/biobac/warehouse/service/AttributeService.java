package com.biobac.warehouse.service;

import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.response.AttributeDefResponse;

import java.util.List;
import java.util.Set;

public interface AttributeService {
    void createValuesForIngredient(Ingredient ingredient, List<AttributeUpsertRequest> attributes);

    List<AttributeDefResponse> getValuesForIngredient(Long ingredientId);

    AttributeDefResponse createAttributeDefinition(AttributeDefRequest request);

    List<AttributeDefResponse> getDefinitionsByGroups(List<Long> attributeGroupIds);

    Set<AttributeDataType> getAttributeDataTypes();
}
