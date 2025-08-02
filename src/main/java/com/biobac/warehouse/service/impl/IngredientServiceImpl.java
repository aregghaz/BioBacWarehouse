
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
        Ingredient entity = mapper.toEntity(dto);
        if (dto.getGroupId() != null) {
            IngredientGroup group = groupRepo.findById(dto.getGroupId()).orElseThrow();
            entity.setGroup(group);
        }
        
        // Save the ingredient first to get its ID
        Ingredient savedIngredient = ingredientRepo.save(entity);
        
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
        return mapper.toDto(ingredientRepo.save(existing));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        ingredientRepo.deleteById(id);
    }
}
