
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientComponentDto;
import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientComponent;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientComponentMapper;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.IngredientComponentRepository;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepo;
    private final IngredientGroupRepository groupRepo;
    private final IngredientComponentRepository componentRepo;
    private final IngredientMapper mapper;
    private final IngredientComponentMapper componentMapper;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    @Override
    public List<IngredientDto> getAll() {
        return ingredientRepo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public IngredientDto getById(Long id) {
        return mapper.toDto(ingredientRepo.findById(id).orElseThrow());
    }

    @Transactional
    @Override
    public IngredientDto create(IngredientDto dto) {
        // Use the mapper to convert DTO to entity, which will handle group
        Ingredient entity = mapper.toEntity(dto);
        
        // Replace the placeholder group with actual entity from the database
        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId()).orElseThrow();
            entity.setGroup(group);
        }
        
        // Save the ingredient first to get its ID
        Ingredient savedIngredient = ingredientRepo.save(entity);
        
        // Handle child ingredient components if provided
        if (dto.getChildIngredientComponents() != null && !dto.getChildIngredientComponents().isEmpty()) {
            for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                // Get the child ingredient
                Ingredient childIngredient = ingredientRepo.findById(componentDto.getChildIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("Child ingredient not found: " + componentDto.getChildIngredientId()));
                
                // Create a new component entity using the constructor
                IngredientComponent component = new IngredientComponent(
                    savedIngredient, 
                    childIngredient, 
                    componentDto.getQuantity()
                );
                
                // Save the component
                componentRepo.save(component);
                
                // Decrease inventory quantity for the child ingredient
                if (dto.getInitialQuantity() != null && dto.getWarehouseId() != null) {
                    // Get the inventory items for this child ingredient
                    List<InventoryItemDto> inventoryItems = inventoryService.findByIngredientId(childIngredient.getId());
                    
                    // Find the inventory item in the same warehouse
                    for (InventoryItemDto inventoryItem : inventoryItems) {
                        if (inventoryItem.getWarehouseId().equals(dto.getWarehouseId())) {
                            // Calculate the amount to decrease
                            int amountToDecrease = (int) Math.ceil(componentDto.getQuantity() * dto.getInitialQuantity());
                            
                            // Check if there's enough inventory
                            if (inventoryItem.getQuantity() < amountToDecrease) {
                                throw new IllegalStateException("Not enough inventory for ingredient: " + childIngredient.getName() + 
                                    ". Required: " + amountToDecrease + ", Available: " + inventoryItem.getQuantity());
                            }
                            
                            // Update the inventory quantity
                            inventoryItem.setQuantity(inventoryItem.getQuantity() - amountToDecrease);
                            inventoryItem.setLastUpdated(LocalDate.now());
                            inventoryService.update(inventoryItem.getId(), inventoryItem);
                            break;
                        }
                    }
                }
            }
            
            // Refresh the saved ingredient to include updated child components
            savedIngredient = ingredientRepo.findById(savedIngredient.getId()).orElseThrow();
        }
        
        
        // Create inventory item if initialQuantity and warehouseId are provided
        if (dto.getInitialQuantity() != null && dto.getWarehouseId() != null) {
            InventoryItemDto inventoryItemDto = new InventoryItemDto();
            inventoryItemDto.setIngredientId(savedIngredient.getId());
            inventoryItemDto.setWarehouseId(dto.getWarehouseId());
            inventoryItemDto.setQuantity(dto.getInitialQuantity());
            inventoryItemDto.setLastUpdated(LocalDate.now());
            inventoryService.create(inventoryItemDto);
        }
        
        return mapper.toDto(savedIngredient);
    }

    @Transactional
    @Override
    public IngredientDto update(Long id, IngredientDto dto) {
        Ingredient existing = ingredientRepo.findById(id).orElseThrow();
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setUnit(dto.getUnit());
        existing.setActive(dto.isActive());
        existing.setQuantity(dto.getQuantity());

        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId()).orElseThrow();
            existing.setGroup(group);
        }

        // Save the updated ingredient
        Ingredient savedIngredient = ingredientRepo.save(existing);
        
        // Handle child ingredient components if provided
        if (dto.getChildIngredientComponents() != null) {
            // Get current child components
            List<IngredientComponent> currentComponents = componentRepo.findByParentIngredientId(id);
            
            // Delete components that are no longer in the list
            for (IngredientComponent currentComponent : currentComponents) {
                boolean found = false;
                for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                    if (componentDto.getId() != null && componentDto.getId().equals(currentComponent.getId())) {
                        found = true;
                        break;
                    }
                    if (componentDto.getChildIngredientId().equals(currentComponent.getChildIngredient().getId())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    componentRepo.delete(currentComponent);
                }
            }
            
            // Add or update components
            for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                IngredientComponent component;
                
                // Check if this is an existing component or a new one
                if (componentDto.getId() != null) {
                    component = componentRepo.findById(componentDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Component not found: " + componentDto.getId()));
                    
                    // Update the quantity
                    component.setQuantity(componentDto.getQuantity());
                } else {
                    // Get the child ingredient
                    Ingredient childIngredient = ingredientRepo.findById(componentDto.getChildIngredientId())
                        .orElseThrow(() -> new IllegalArgumentException("Child ingredient not found: " + componentDto.getChildIngredientId()));
                    
                    // Check for self-reference
                    if (childIngredient.getId().equals(id)) {
                        throw new IllegalArgumentException("An ingredient cannot be its own child");
                    }
                    
                    // Create a new component using the constructor
                    component = new IngredientComponent(
                        savedIngredient,
                        childIngredient,
                        componentDto.getQuantity()
                    );
                }
                
                // Save the component
                componentRepo.save(component);
            }
        }
        
        
        // Refresh the saved ingredient to include updated components
        savedIngredient = ingredientRepo.findById(savedIngredient.getId()).orElseThrow();
        
        return mapper.toDto(savedIngredient);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ingredientRepo.deleteById(id);
    }
}
