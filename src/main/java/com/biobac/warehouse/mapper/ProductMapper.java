package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.response.ProductTableResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RecipeItemMapper.class})
public abstract class ProductMapper {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", expression = "java(mapIngredientListToIdList(product.getIngredients()))"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "quantity", ignore = true), // will be set manually in service
            @Mapping(target = "warehouseId", ignore = true), // will be set manually in service
            @Mapping(target = "companyId", source = "companyId")
    })
    public abstract ProductDto toDto(Product product);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "companyId", source = "companyId"),
            @Mapping(target = "ingredients", expression = "java(mapIdListToIngredientList(dto.getIngredientIds()))"),
            @Mapping(target = "recipeItems", ignore = true),
            @Mapping(target = "inventoryItems", ignore = true)
    })
    public abstract Product toEntity(ProductDto dto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "companyId", source = "companyId"),
            @Mapping(target = "ingredients", expression = "java(mapIdListToIngredientList(dto.getIngredientIds()))"),
            @Mapping(target = "recipeItems", ignore = true),
            @Mapping(target = "inventoryItems", ignore = true)
    })
    public abstract Product toEntity(ProductCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ingredients", expression = "java(mapIdListToIngredientList(dto.getIngredientIds()))"),
            @Mapping(target = "recipeItems", ignore = true),
            @Mapping(target = "inventoryItems", ignore = true)
    })
    public abstract void updateEntityFromDto(ProductDto dto, @MappingTarget Product entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "ingredients", expression = "java(mapIdListToIngredientList(dto.getIngredientIds()))"),
            @Mapping(target = "recipeItems", ignore = true),
            @Mapping(target = "inventoryItems", ignore = true)
    })
    public abstract void updateEntityFromUpdateRequest(ProductUpdateRequest dto, @MappingTarget Product entity);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", expression = "java(mapIngredientListToIdList(entity.getIngredients()))"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "quantity", ignore = true), // will be set manually in service
            @Mapping(target = "warehouseName", expression = "java(getWarehouseName(entity))")
    })
    public abstract ProductTableResponse toTableResponse(Product entity);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", expression = "java(mapIngredientListToIdList(entity.getIngredients()))"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "quantity", ignore = true),
            @Mapping(target = "warehouseId", ignore = true),
            @Mapping(target = "companyId", source = "companyId")
    })
    public abstract ProductResponse toResponse(Product entity);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", source = "ingredientIds"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "warehouseId", source = "warehouseId"),
            @Mapping(target = "companyId", source = "companyId")
    })
    public abstract ProductResponse toResponse(ProductDto dto);

    protected List<Long> mapIngredientListToIdList(List<Ingredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream().map(Ingredient::getId).collect(Collectors.toList());
    }

    protected List<Ingredient> mapIdListToIngredientList(List<Long> ids) {
        if (ids == null) return null;
        return ingredientRepository.findAllById(ids);
    }

    protected String getWarehouseName(Product entity) {
        if (entity == null || entity.getId() == null) {
            return null;
        }

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByProductId(entity.getId());
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return null;
        }

        InventoryItem item = inventoryItems.get(0);

        // Prefer the relationship; it is the authoritative source for the name
        if (item.getWarehouse() != null) {
            return item.getWarehouse().getName();
        }

        // If relation is not initialized, we cannot derive a name safely just from an ID
        return null;
    }
}
