
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.mapper.InventoryMapper;
import com.biobac.warehouse.repository.IngredientGroupRepository;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.WarehouseRepository;
import com.biobac.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryItemRepository repo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
    private final IngredientGroupRepository ingredientGroupRepo;
    private final WarehouseRepository warehouseRepo;
    private final InventoryMapper mapper;

    public List<InventoryItemDto> getAll() {
        return repo.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public InventoryItemDto getById(Long id) {
        return mapper.toDto(repo.findById(id).orElseThrow());
    }

    public InventoryItemDto create(InventoryItemDto dto) {
        InventoryItem entity = mapper.toEntity(dto);
        if (dto.getProductId() != null) {
            entity.setProduct(productRepo.findById(dto.getProductId()).orElse(null));
        }
        if (dto.getIngredientId() != null) {
            entity.setIngredient(ingredientRepo.findById(dto.getIngredientId()).orElse(null));
        }
        if (dto.getIngredientGroupId() != null) {
            entity.setIngredientGroup(ingredientGroupRepo.findById(dto.getIngredientGroupId()).orElse(null));
            entity.setGroupId(dto.getIngredientGroupId());
        }
        if (dto.getWarehouseId() != null) {
            entity.setWarehouse(warehouseRepo.findById(dto.getWarehouseId()).orElse(null));
            entity.setWarehouseId(dto.getWarehouseId());
        }
        return mapper.toDto(repo.save(entity));
    }

    public InventoryItemDto update(Long id, InventoryItemDto dto) {
        InventoryItem item = repo.findById(id).orElseThrow();
        item.setQuantity(dto.getQuantity());
        item.setLastUpdated(dto.getLastUpdated());
        
        // Update ingredient count if provided
        if (dto.getIngredientCount() != null) {
            item.setIngredientCount(dto.getIngredientCount());
        }
        
        if (dto.getProductId() != null) {
            item.setProduct(productRepo.findById(dto.getProductId()).orElse(null));
        }
        if (dto.getIngredientId() != null) {
            item.setIngredient(ingredientRepo.findById(dto.getIngredientId()).orElse(null));
        }
        if (dto.getIngredientGroupId() != null) {
            item.setIngredientGroup(ingredientGroupRepo.findById(dto.getIngredientGroupId()).orElse(null));
            item.setGroupId(dto.getIngredientGroupId());
        }
        if (dto.getWarehouseId() != null) {
            item.setWarehouse(warehouseRepo.findById(dto.getWarehouseId()).orElse(null));
            item.setWarehouseId(dto.getWarehouseId());
        }
        
        return mapper.toDto(repo.save(item));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
    
    @Override
    public List<InventoryItemDto> findByProductId(Long productId) {
        return repo.findByProductId(productId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryItemDto> findByIngredientId(Long ingredientId) {
        return repo.findByIngredientId(ingredientId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryItemDto> findByWarehouseId(Long warehouseId) {
        return repo.findByWarehouseId(warehouseId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryItemDto> findByGroupId(Long groupId) {
        return repo.findByGroupId(groupId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
