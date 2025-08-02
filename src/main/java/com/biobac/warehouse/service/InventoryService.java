
package com.biobac.warehouse.service;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.Warehouse;
import com.biobac.warehouse.mapper.InventoryMapper;
import com.biobac.warehouse.repository.IngredientRepository;
import com.biobac.warehouse.repository.InventoryItemRepository;
import com.biobac.warehouse.repository.ProductRepository;
import com.biobac.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryItemRepository repo;
    private final ProductRepository productRepo;
    private final IngredientRepository ingredientRepo;
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
        entity.setProduct(productRepo.findById(dto.getProductId()).orElse(null));
        entity.setIngredient(ingredientRepo.findById(dto.getIngredientId()).orElse(null));
        entity.setWarehouse(warehouseRepo.findById(dto.getWarehouseId()).orElse(null));
        return mapper.toDto(repo.save(entity));
    }

    public InventoryItemDto update(Long id, InventoryItemDto dto) {
        InventoryItem item = repo.findById(id).orElseThrow();
        item.setQuantity(dto.getQuantity());
        item.setLastUpdated(dto.getLastUpdated());
        return mapper.toDto(repo.save(item));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
