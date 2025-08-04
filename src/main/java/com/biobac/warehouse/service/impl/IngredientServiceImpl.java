
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientComponentDto;
import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientComponent;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientComponentMapper;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.IngredientComponentRepository;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.service.IngredientHistoryService;
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
    private final IngredientHistoryService historyService;

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
        // Debug log the incoming DTO
        System.out.println("[DEBUG_LOG] Creating ingredient with name: " + dto.getName() + 
            ", quantity: " + dto.getQuantity() + 
            ", warehouseId: " + dto.getWarehouseId());
        
        // Use the mapper to convert DTO to entity, which will handle group
        Ingredient entity = mapper.toEntity(dto);
        
        // Replace the placeholder group with actual entity from the database
        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient group not found with ID: " + dto.getGroupId()));
            entity.setGroup(group);
        }
        
        // Save the ingredient first to get its ID
        Ingredient savedIngredient = ingredientRepo.save(entity);
        
        // Record history for ingredient creation
        historyService.recordQuantityChange(
            savedIngredient, 
            0.0, 
            savedIngredient.getQuantity(), 
            "CREATED", 
            "Initial creation of ingredient");
        
        // Handle child ingredient components if provided
        if (dto.getChildIngredientComponents() != null && !dto.getChildIngredientComponents().isEmpty()) {
            for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                // Skip if childIngredientId is null
                if (componentDto.getChildIngredientId() == null) {
                    continue;
                }
                
                // Get the child ingredient
                Ingredient childIngredient = ingredientRepo.findById(componentDto.getChildIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("Child ingredient not found: " + componentDto.getChildIngredientId()));
                
                // Create a new component entity using the constructor
                IngredientComponent component = new IngredientComponent(
                    savedIngredient, 
                    childIngredient, 
                    componentDto.getQuantity() != null ? componentDto.getQuantity() : 0.0
                );
                
                // Save the component
                componentRepo.save(component);
                
                // Decrease inventory quantity for the child ingredient only if all required values are present
                if (dto.getWarehouseId() != null && componentDto.getQuantity() != null) {
                    // Use quantity for calculations
                    Double quantityToUse = null;
                    if (dto.getQuantity() != null) {
                        quantityToUse = dto.getQuantity();
                        System.out.println("[DEBUG_LOG] Using quantity: " + quantityToUse);
                    } else {
                        System.out.println("[DEBUG_LOG] No quantity available, skipping inventory reduction");
                        continue;
                    }
                    try {
                        // Get the inventory items for this child ingredient
                        List<InventoryItemDto> inventoryItems = inventoryService.findByIngredientId(childIngredient.getId());
                        
                        // Skip if no inventory items found
                        if (inventoryItems == null || inventoryItems.isEmpty()) {
                            continue;
                        }
                        
                        // Find the inventory item in the same warehouse
                        boolean foundMatchingWarehouse = false;
                        for (InventoryItemDto inventoryItem : inventoryItems) {
                            // Skip items with null warehouseId or id
                            if (inventoryItem == null || inventoryItem.getWarehouseId() == null || 
                                inventoryItem.getId() == null || dto.getWarehouseId() == null) {
                                continue;
                            }
                            
                            if (inventoryItem.getWarehouseId().equals(dto.getWarehouseId())) {
                                foundMatchingWarehouse = true;
                                
                                // Calculate the amount to decrease
                                int amountToDecrease = (int) Math.ceil(componentDto.getQuantity() * quantityToUse);
                                
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
                        
                        // If no matching warehouse was found, log it but continue
                        if (!foundMatchingWarehouse) {
                            System.out.println("Warning: No inventory found for ingredient " + childIngredient.getName() + 
                                " in warehouse " + dto.getWarehouseId());
                        }
                    } catch (Exception e) {
                        // Log the error but continue processing
                        System.err.println("Error processing inventory for child ingredient " + childIngredient.getName() + 
                            ": " + e.getMessage());
                    }
                }
            }
            
            // Refresh the saved ingredient to include updated child components
            savedIngredient = ingredientRepo.findById(savedIngredient.getId()).orElseThrow();
        }
        
        
        // Create inventory item if quantity and warehouseId are provided
        if (dto.getQuantity() != null && dto.getWarehouseId() != null) {
            // Use quantity for calculations
            Double quantityForCalculations = null;
            System.out.println("[DEBUG_LOG] Determining quantity - quantity: " + dto.getQuantity());
            if (dto.getQuantity() != null) {
                quantityForCalculations = dto.getQuantity();
                System.out.println("[DEBUG_LOG] Creating inventory item with quantity: " + dto.getQuantity());
            } else {
                System.out.println("[DEBUG_LOG] No quantity available for inventory item");
                quantityForCalculations = 0.0;
            }
            System.out.println("[DEBUG_LOG] Final quantityForCalculations: " + quantityForCalculations);
            // First, check if this ingredient is created from another ingredient
            // If so, decrease the inventory of the source ingredient
            if (dto.getChildIngredientComponents() != null && !dto.getChildIngredientComponents().isEmpty()) {
                for (IngredientComponentDto componentDto : dto.getChildIngredientComponents()) {
                    if (componentDto.getChildIngredientId() != null) {
                        try {
                            // Get inventory items for the child ingredient
                            List<InventoryItemDto> inventoryItems = inventoryService.findByIngredientId(componentDto.getChildIngredientId());
                            
                            if (inventoryItems != null && !inventoryItems.isEmpty()) {
                                for (InventoryItemDto inventoryItem : inventoryItems) {
                                    if (inventoryItem != null && inventoryItem.getWarehouseId() != null && 
                                        inventoryItem.getId() != null && dto.getWarehouseId().equals(inventoryItem.getWarehouseId())) {
                                        
                                        // Decrease the quantity by the quantity of the new ingredient
                                        int amountToDecrease = (int) Math.ceil(quantityForCalculations);
                                        
                                        // Check if there's enough inventory
                                        if (inventoryItem.getQuantity() < amountToDecrease) {
                                            System.out.println("Warning: Not enough inventory for ingredient ID " + 
                                                componentDto.getChildIngredientId() + ". Required: " + amountToDecrease + 
                                                ", Available: " + inventoryItem.getQuantity());
                                        } else {
                                            // Update the inventory quantity
                                            inventoryItem.setQuantity(inventoryItem.getQuantity() - amountToDecrease);
                                            inventoryItem.setLastUpdated(LocalDate.now());
                                            inventoryService.update(inventoryItem.getId(), inventoryItem);
                                            
                                            // Update the child ingredient's quantity field as well
                                            Ingredient childIngredient = ingredientRepo.findById(componentDto.getChildIngredientId()).orElse(null);
                                            if (childIngredient != null && childIngredient.getQuantity() != null) {
                                                double newQuantity = childIngredient.getQuantity() - amountToDecrease;
                                                childIngredient.setQuantity(newQuantity >= 0 ? newQuantity : 0);
                                                ingredientRepo.save(childIngredient);
                                                System.out.println("[DEBUG_LOG] Updated child ingredient quantity to: " + childIngredient.getQuantity());
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error decreasing inventory for source ingredient: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Create inventory item for the new ingredient
            InventoryItemDto inventoryItemDto = new InventoryItemDto();
            inventoryItemDto.setIngredientId(savedIngredient.getId());
            inventoryItemDto.setWarehouseId(dto.getWarehouseId());
            
            // Determine the quantity to use for the inventory item
            Integer quantityToUse;
            
            // Use quantity if available
            if (dto.getQuantity() != null) {
                quantityToUse = (int) Math.ceil(dto.getQuantity());
                System.err.println("Using quantity for inventory: " + quantityToUse);
            }
            // Default to 0 if nothing else is available
            else {
                quantityToUse = 0;
                System.err.println("No quantity available for inventory, using 0");
            }
            
            inventoryItemDto.setQuantity(quantityToUse);
            inventoryItemDto.setLastUpdated(LocalDate.now());
            
            // Set the ingredient count based on the number of child ingredients
            int ingredientCount = 0;
            if (dto.getChildIngredientComponents() != null && !dto.getChildIngredientComponents().isEmpty()) {
                ingredientCount = dto.getChildIngredientComponents().size();
            }
            inventoryItemDto.setIngredientCount(ingredientCount);
            
            inventoryService.create(inventoryItemDto);
        }
        
        return mapper.toDto(savedIngredient);
    }

    @Transactional
    @Override
    public IngredientDto update(Long id, IngredientDto dto) {
        Ingredient existing = ingredientRepo.findById(id).orElseThrow();
        
        // Store the original quantity for history tracking
        Double originalQuantity = existing.getQuantity();
        
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setUnit(dto.getUnit());
        existing.setActive(dto.isActive());
        existing.setQuantity(dto.getQuantity());
        
        // Record history if quantity changed
        if (originalQuantity == null && dto.getQuantity() != null || 
            originalQuantity != null && !originalQuantity.equals(dto.getQuantity())) {
            historyService.recordQuantityChange(
                existing, 
                originalQuantity != null ? originalQuantity : 0.0, 
                dto.getQuantity() != null ? dto.getQuantity() : 0.0, 
                "UPDATED", 
                "Ingredient updated");
        }

        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Ingredient group not found with ID: " + dto.getGroupId()));
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
