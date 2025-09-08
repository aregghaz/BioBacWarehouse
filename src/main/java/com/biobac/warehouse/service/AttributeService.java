package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AttributeDefResponse;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AttributeService {
    AttributeDefResponse getById(Long id);

    void createValuesForIngredient(Ingredient ingredient, List<AttributeUpsertRequest> attributes);

    List<AttributeDefResponse> getValuesForIngredient(Long ingredientId);

    void createValuesForProduct(Product product, List<AttributeUpsertRequest> attributes);

    List<AttributeDefResponse> getValuesForProduct(Long productId);

    void createValuesForWarehouse(Warehouse warehouse, List<AttributeUpsertRequest> attributes);

    List<AttributeDefResponse> getValuesForWarehouse(Long warehouseId);

    AttributeDefResponse createAttributeDefinition(AttributeDefRequest request);

    AttributeDefResponse updateAttributeDefinition(Long id, AttributeDefRequest request);

    void deleteAttributeDefinition(Long id);

    List<AttributeDefResponse> getDefinitionsByGroups(List<Long> attributeGroupIds);

    Pair<List<AttributeDefResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                       Integer page,
                                                                       Integer size,
                                                                       String sortBy,
                                                                       String sortDir);

    Set<AttributeDataType> getAttributeDataTypes();
}
