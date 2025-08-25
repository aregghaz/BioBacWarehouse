package com.biobac.warehouse.mapper;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.request.InventoryItemCreateRequest;
import com.biobac.warehouse.request.InventoryItemUpdateRequest;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.InventoryItemTableResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class InventoryMapper {
    @Mappings({
        @Mapping(target = "productId", source = "product.id"),
        @Mapping(target = "ingredientId", source = "ingredient.id"),
        @Mapping(target = "ingredientGroupId", source = "ingredientGroup.id"),
        @Mapping(target = "warehouseId", source = "warehouse.id"),
        @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract InventoryItemDto toDto(InventoryItem entity);

    @Mappings({
        @Mapping(target = "product", ignore = true),
        @Mapping(target = "ingredient", ignore = true),
        @Mapping(target = "ingredientGroup", ignore = true),
        @Mapping(target = "warehouse", ignore = true),
        @Mapping(target = "warehouseId", source = "warehouseId"),
        @Mapping(target = "groupId", source = "ingredientGroupId"),
        @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract InventoryItem toEntity(InventoryItemDto dto);

    @Mappings({
        @Mapping(target = "product", ignore = true),
        @Mapping(target = "ingredient", ignore = true),
        @Mapping(target = "ingredientGroup", ignore = true),
        @Mapping(target = "warehouse", ignore = true),
        @Mapping(target = "warehouseId", source = "warehouseId"),
        @Mapping(target = "groupId", source = "ingredientGroupId"),
        @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract InventoryItem toEntity(InventoryItemCreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
        @Mapping(target = "product", ignore = true),
        @Mapping(target = "ingredient", ignore = true),
        @Mapping(target = "ingredientGroup", ignore = true),
        @Mapping(target = "warehouse", ignore = true),
        @Mapping(target = "warehouseId", source = "warehouseId"),
        @Mapping(target = "groupId", source = "ingredientGroupId"),
        @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract void updateEntityFromRequest(InventoryItemUpdateRequest dto, @MappingTarget InventoryItem entity);

    @AfterMapping
    protected void setIngredientGroup(@MappingTarget InventoryItem entity, InventoryItemDto dto) {
        if (dto.getIngredientGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getIngredientGroupId());
            entity.setIngredientGroup(group);
        }
    }

    @AfterMapping
    protected void setIngredientGroup(@MappingTarget InventoryItem entity, InventoryItemCreateRequest dto) {
        if (dto.getIngredientGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getIngredientGroupId());
            entity.setIngredientGroup(group);
        }
    }

    @AfterMapping
    protected void setIngredientGroup(@MappingTarget InventoryItem entity, InventoryItemUpdateRequest dto) {
        if (dto.getIngredientGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getIngredientGroupId());
            entity.setIngredientGroup(group);
        }
    }

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "quantity", source = "quantity"),
            @Mapping(target = "lastUpdated", ignore = true),
            @Mapping(target = "productName", source = "product.name"),
            @Mapping(target = "ingredientGroupName", source = "ingredientGroup.name"),
            @Mapping(target = "ingredientName", source = "ingredient.name"),
            @Mapping(target = "warehouseName", source = "warehouse.name"),
            @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract InventoryItemTableResponse toTableResponse(InventoryItem inventoryItem);

    @Mappings({
        @Mapping(target = "productId", source = "product.id"),
        @Mapping(target = "ingredientId", source = "ingredient.id"),
        @Mapping(target = "ingredientGroupId", source = "ingredientGroup.id"),
        @Mapping(target = "warehouseId", source = "warehouseId"),
        @Mapping(target = "ingredientCount", source = "ingredientCount")
    })
    public abstract InventoryItemResponse toResponse(InventoryItem entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "productId", source = "productId"),
        @Mapping(target = "ingredientId", source = "ingredientId"),
        @Mapping(target = "ingredientGroupId", source = "ingredientGroupId"),
        @Mapping(target = "warehouseId", source = "warehouseId"),
        @Mapping(target = "quantity", source = "quantity"),
        @Mapping(target = "ingredientCount", source = "ingredientCount"),
        @Mapping(target = "lastUpdated", source = "lastUpdated")
    })
    public abstract InventoryItemResponse toResponse(InventoryItemDto dto);
}
