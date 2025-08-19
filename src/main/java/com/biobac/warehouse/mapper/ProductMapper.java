package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.ProductDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.response.ProductTableResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RecipeItemMapper.class})
public abstract class ProductMapper {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredientIds", expression = "java(mapIngredientListToIdList(product.getIngredients()))"),
            @Mapping(target = "recipeItems", source = "recipeItems"),
            @Mapping(target = "quantity", ignore = true), // will be set manually in service
            @Mapping(target = "warehouseId", ignore = true) // will be set manually in service
    })
    public abstract ProductDto toDto(Product product);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "description", source = "description"),
            @Mapping(target = "sku", source = "sku"),
            @Mapping(target = "ingredients", ignore = true), // manually set later in service
            @Mapping(target = "recipeItems", ignore = true),  // manually set later in service
            @Mapping(target = "inventoryItems", ignore = true)
            // quantity and warehouseId are not entity fields, they're only in the DTO
    })
    public abstract Product toEntity(ProductDto dto);

    protected List<Long> mapIngredientListToIdList(List<Ingredient> ingredients) {
        if (ingredients == null) return null;
        return ingredients.stream().map(Ingredient::getId).collect(Collectors.toList());
    }

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

    protected String getWarehouseName(Product entity) {
        if (entity.getId() == null) {
            return null;
        }

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByProductId(entity.getId());
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return null;
        }

        InventoryItem item = inventoryItems.get(0);

        // First try to get warehouseId directly from the field
        if (item.getWarehouseId() != null) {
            return item.getWarehouse().getName();
        }

        // If that's null, try to get it from the warehouse relationship
        if (item.getWarehouse() != null) {
            return item.getWarehouse().getName();
        }

        return null;
    }
}
