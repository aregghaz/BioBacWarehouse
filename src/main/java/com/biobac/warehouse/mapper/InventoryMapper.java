package com.biobac.warehouse.mapper;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.entity.InventoryItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mappings({
        @Mapping(target = "productId", source = "product.id"),
        @Mapping(target = "ingredientId", source = "ingredient.id"),
        @Mapping(target = "ingredientGroupId", source = "ingredientGroup.id"),
        @Mapping(target = "warehouseId", source = "warehouse.id")
    })
    InventoryItemDto toDto(InventoryItem entity);
    
    @Mappings({
        @Mapping(target = "product", ignore = true),
        @Mapping(target = "ingredient", ignore = true),
        @Mapping(target = "ingredientGroup", ignore = true),
        @Mapping(target = "warehouse", ignore = true)
    })
    InventoryItem toEntity(InventoryItemDto dto);
    
    @AfterMapping
    default void setIngredientGroup(@MappingTarget InventoryItem entity, InventoryItemDto dto) {
        if (dto.getIngredientGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getIngredientGroupId());
            entity.setIngredientGroup(group);
        }
    }
}
