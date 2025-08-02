
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientDto;
import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientGroup;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepo;
    private final IngredientGroupRepository groupRepo;
    private final IngredientMapper mapper;
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
        // Use the mapper to convert DTO to entity, which will handle group and parent ingredient
        Ingredient entity = mapper.toEntity(dto);
        
        // Replace the placeholder group and parent ingredient with actual entities from the database
        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId()).orElseThrow();
            entity.setGroup(group);
        }
        
        if (dto.getParentIngredientId() != null) {
            Ingredient parentIngredient = ingredientRepo.findById(dto.getParentIngredientId()).orElseThrow();
            entity.setParentIngredient(parentIngredient);
        }
        
        // Save the ingredient first to get its ID
        Ingredient savedIngredient = ingredientRepo.save(entity);
        
        // Handle child ingredients if provided
        if (dto.getChildIngredientIds() != null && !dto.getChildIngredientIds().isEmpty()) {
            List<Ingredient> childIngredients = ingredientRepo.findAllById(dto.getChildIngredientIds());
            for (Ingredient childIngredient : childIngredients) {
                childIngredient.setParentIngredient(savedIngredient);
                ingredientRepo.save(childIngredient);
            }
            // Refresh the saved ingredient to include updated child ingredients
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

        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId()).orElseThrow();
            existing.setGroup(group);
        }

        // Handle parent ingredient relationship
        if (dto.getParentIngredientId() != null) {
            // Prevent self-referencing
            if (dto.getParentIngredientId().equals(id)) {
                throw new IllegalArgumentException("An ingredient cannot be its own parent");
            }

            Ingredient parentIngredient = ingredientRepo.findById(dto.getParentIngredientId()).orElseThrow();
            existing.setParentIngredient(parentIngredient);
        } else {
            existing.setParentIngredient(null);
        }
        
        // Save the updated ingredient
        Ingredient savedIngredient = ingredientRepo.save(existing);
        
        // Handle child ingredients if provided
        if (dto.getChildIngredientIds() != null) {
            // First, remove parent reference from current children that are not in the new list
            if (existing.getChildIngredients() != null) {
                for (Ingredient child : existing.getChildIngredients()) {
                    if (!dto.getChildIngredientIds().contains(child.getId())) {
                        child.setParentIngredient(null);
                        ingredientRepo.save(child);
                    }
                }
            }
            
            // Then set parent reference for new children
            if (!dto.getChildIngredientIds().isEmpty()) {
                List<Ingredient> childIngredients = ingredientRepo.findAllById(dto.getChildIngredientIds());
                for (Ingredient childIngredient : childIngredients) {
                    // Prevent circular references
                    if (childIngredient.getId().equals(id)) {
                        throw new IllegalArgumentException("An ingredient cannot be its own child");
                    }
                    childIngredient.setParentIngredient(savedIngredient);
                    ingredientRepo.save(childIngredient);
                }
            }
            
            // Refresh the saved ingredient to include updated child ingredients
            savedIngredient = ingredientRepo.findById(savedIngredient.getId()).orElseThrow();
        }

        return mapper.toDto(savedIngredient);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ingredientRepo.deleteById(id);
    }
}
