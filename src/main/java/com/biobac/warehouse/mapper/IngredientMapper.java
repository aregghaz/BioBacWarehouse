package com.biobac.warehouse.mapper;

import com.biobac.warehouse.dto.IngredientComponentDto;
import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientComponent;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.repository.InventoryItemRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {IngredientComponentMapper.class})
public abstract class IngredientMapper {

    @Autowired
    private IngredientComponentMapper componentMapper;
    
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "quantity", source = "quantity"),
        @Mapping(target = "groupId", source = "group.id"),
        @Mapping(target = "initialQuantity", expression = "java(getExactQuantity(entity))"),
        @Mapping(target = "warehouseId", expression = "java(getWarehouseId(entity))"),
        @Mapping(target = "childIngredientComponents", expression = "java(mapChildIngredientComponents(entity))")
    })
    public abstract IngredientDto toDto(Ingredient entity);

    @Mappings({
        @Mapping(target = "id", source = "id"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "description", source = "description"),
        @Mapping(target = "unit", source = "unit"),
        @Mapping(target = "active", source = "active"),
        @Mapping(target = "quantity", source = "quantity"),
        @Mapping(target = "group", ignore = true),
        @Mapping(target = "childIngredientComponents", ignore = true),
        @Mapping(target = "recipeItems", ignore = true)
    })
    public abstract Ingredient toEntity(IngredientDto dto);

    @AfterMapping
    protected void setGroup(@MappingTarget Ingredient entity, IngredientDto dto) {
        if (dto.getGroupId() != null) {
            IngredientGroup group = new IngredientGroup();
            group.setId(dto.getGroupId());
            entity.setGroup(group);
        }
    }

    @AfterMapping
    protected void setChildIngredientComponents(@MappingTarget Ingredient entity, IngredientDto dto) {
        if (dto.getChildIngredientComponents() != null && !dto.getChildIngredientComponents().isEmpty()) {
            List<IngredientComponent> components = new ArrayList<>();
            for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                IngredientComponent component = componentMapper.toEntity(componentDto);
                component.setParentIngredient(entity);
                components.add(component);
            }
            entity.setChildIngredientComponents(components);
        }
    }

    protected List<IngredientComponentDto> mapChildIngredientComponents(Ingredient entity) {
        if (entity.getChildIngredientComponents() == null) {
            return new ArrayList<>();
        }
        return entity.getChildIngredientComponents().stream()
                .map(componentMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * @deprecated Use getExactQuantity instead
     */
    @Deprecated
    protected Integer getInitialQuantity(Ingredient entity) {
        if (entity.getId() == null) {
            return null;
        }
        
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByIngredientId(entity.getId());
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return null;
        }
        
        // Return the quantity of the first inventory item
        return inventoryItems.get(0).getQuantity();
    }
    
    /**
     * Gets the exact total quantity by summing all inventory items for this ingredient
     */
    protected Integer getExactQuantity(Ingredient entity) {
        if (entity.getId() == null) {
            System.out.println("[DEBUG_LOG] getExactQuantity: entity.getId() is null");
            return null;
        }
        
        System.out.println("[DEBUG_LOG] getExactQuantity: Looking for inventory items for ingredient ID: " + entity.getId());
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByIngredientId(entity.getId());
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            System.out.println("[DEBUG_LOG] getExactQuantity: No inventory items found");
            return null;
        }
        
        System.out.println("[DEBUG_LOG] getExactQuantity: Found " + inventoryItems.size() + " inventory items");
        // Log each inventory item's quantity
        for (InventoryItem item : inventoryItems) {
            System.out.println("[DEBUG_LOG] Inventory item ID: " + item.getId() + ", Quantity: " + item.getQuantity());
        }
        
        // Sum quantities across all inventory items to get exact total quantity
        int totalQuantity = inventoryItems.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
        
        System.out.println("[DEBUG_LOG] getExactQuantity: Total quantity: " + totalQuantity);
        return totalQuantity;
    }
    
    protected Long getWarehouseId(Ingredient entity) {
        if (entity.getId() == null) {
            return null;
        }
        
        List<InventoryItem> inventoryItems = inventoryItemRepository.findByIngredientId(entity.getId());
        if (inventoryItems == null || inventoryItems.isEmpty()) {
            return null;
        }
        
        InventoryItem item = inventoryItems.get(0);
        
        // First try to get warehouseId directly from the field
        if (item.getWarehouseId() != null) {
            return item.getWarehouseId();
        }
        
        // If that's null, try to get it from the warehouse relationship
        if (item.getWarehouse() != null) {
            return item.getWarehouse().getId();
        }
        
        return null;
    }
}
