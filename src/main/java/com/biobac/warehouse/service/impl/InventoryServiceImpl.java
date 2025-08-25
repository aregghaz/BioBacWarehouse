
package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.InventoryItemDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.InventoryItem;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.InventoryMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryItemCreateRequest;
import com.biobac.warehouse.request.InventoryItemUpdateRequest;
import com.biobac.warehouse.response.InventoryItemTableResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.InventoryService;
import com.biobac.warehouse.utils.specifications.InventoryItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
    private final IngredientHistoryService historyService;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> getAll() {
        return repo.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryItemTableResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                    Integer page,
                                                                                    Integer size,
                                                                                    String sortBy,
                                                                                    String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters);

        Page<InventoryItem> inventoryItemPage = repo.findAll(spec, pageable);

        List<InventoryItemTableResponse> content = inventoryItemPage.getContent()
                .stream()
                .map(mapper::toTableResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                inventoryItemPage.getNumber(),
                inventoryItemPage.getSize(),
                inventoryItemPage.getTotalElements(),
                inventoryItemPage.getTotalPages(),
                inventoryItemPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "inventoryItemTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemDto getById(Long id) {
        return mapper.toDto(repo.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public InventoryItemDto create(InventoryItemDto dto) {
        InventoryItem entity = mapper.toEntity(dto);

        // Set references
        if (dto.getProductId() != null) {
            entity.setProduct(productRepo.findById(dto.getProductId()).orElse(null));
        }

        Ingredient ingredient = null;
        if (dto.getIngredientId() != null) {
            ingredient = ingredientRepo.findById(dto.getIngredientId()).orElse(null);
            entity.setIngredient(ingredient);
        }

        if (dto.getIngredientGroupId() != null) {
            entity.setIngredientGroup(ingredientGroupRepo.findById(dto.getIngredientGroupId()).orElse(null));
            entity.setGroupId(dto.getIngredientGroupId());
        }

        if (dto.getWarehouseId() != null) {
            entity.setWarehouse(warehouseRepo.findById(dto.getWarehouseId()).orElse(null));
            entity.setWarehouseId(dto.getWarehouseId());
        }

        // Save the entity
        InventoryItem savedEntity = repo.save(entity);

        // Record history if this is an ingredient with quantity
        if (ingredient != null && dto.getQuantity() != null) {
            // Record the history for inventory creation
            historyService.recordQuantityChange(
                    ingredient,
                    0.0,
                    dto.getQuantity().doubleValue(),
                    "INVENTORY_CREATED",
                    "New inventory item created"
            );
        }

        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public InventoryItemDto create(InventoryItemCreateRequest dto) {
        InventoryItem entity = mapper.toEntity(dto);

        // Set references
        if (dto.getProductId() != null) {
            entity.setProduct(productRepo.findById(dto.getProductId()).orElse(null));
        }

        Ingredient ingredient = null;
        if (dto.getIngredientId() != null) {
            ingredient = ingredientRepo.findById(dto.getIngredientId()).orElse(null);
            entity.setIngredient(ingredient);
        }

        if (dto.getIngredientGroupId() != null) {
            entity.setIngredientGroup(ingredientGroupRepo.findById(dto.getIngredientGroupId()).orElse(null));
            entity.setGroupId(dto.getIngredientGroupId());
        }

        if (dto.getWarehouseId() != null) {
            entity.setWarehouse(warehouseRepo.findById(dto.getWarehouseId()).orElse(null));
            entity.setWarehouseId(dto.getWarehouseId());
        }

        // Save the entity
        InventoryItem savedEntity = repo.save(entity);

        // Record history if this is an ingredient with quantity
        if (ingredient != null && dto.getQuantity() != null) {
            historyService.recordQuantityChange(
                    ingredient,
                    0.0,
                    dto.getQuantity().doubleValue(),
                    "INVENTORY_CREATED",
                    "New inventory item created"
            );
        }

        return mapper.toDto(savedEntity);
    }

    @Override
    @Transactional
    public InventoryItemDto update(Long id, InventoryItemDto dto) {
        InventoryItem item = repo.findById(id).orElseThrow();

        // Store original quantity for history tracking
        Integer originalQuantity = item.getQuantity();

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

        // Save the updated item
        InventoryItem savedItem = repo.save(item);

        // Record history if this is an ingredient and quantity changed
        if (savedItem.getIngredient() != null &&
                (originalQuantity == null && dto.getQuantity() != null ||
                        originalQuantity != null && !originalQuantity.equals(dto.getQuantity()))) {

            Ingredient ingredient = savedItem.getIngredient();

            // Record the history for inventory update
            historyService.recordQuantityChange(
                    ingredient,
                    originalQuantity != null ? originalQuantity.doubleValue() : 0.0,
                    dto.getQuantity().doubleValue(),
                    "INVENTORY_UPDATE",
                    "Inventory quantity updated"
            );
        }

        return mapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public InventoryItemDto update(Long id, InventoryItemUpdateRequest dto) {
        InventoryItem item = repo.findById(id).orElseThrow();

        Integer originalQuantity = item.getQuantity();

        // Map non-null fields from request onto entity
        mapper.updateEntityFromRequest(dto, item);

        // Relationships based on IDs
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

        InventoryItem savedItem = repo.save(item);

        if (savedItem.getIngredient() != null &&
                (originalQuantity == null && dto.getQuantity() != null ||
                        originalQuantity != null && !originalQuantity.equals(dto.getQuantity()))) {
            Ingredient ingredient = savedItem.getIngredient();
            historyService.recordQuantityChange(
                    ingredient,
                    originalQuantity != null ? originalQuantity.doubleValue() : 0.0,
                    dto.getQuantity() != null ? dto.getQuantity().doubleValue() : (savedItem.getQuantity() != null ? savedItem.getQuantity().doubleValue() : 0.0),
                    "INVENTORY_UPDATE",
                    "Inventory quantity updated"
            );
        }

        return mapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Load the inventory item or throw 404 if not found
        InventoryItem item = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("InventoryItem not found with id: " + id));

        if (item.getIngredient() != null && item.getQuantity() != null) {
            Ingredient ingredient = item.getIngredient();

            // Record the history before deleting
            historyService.recordQuantityChange(
                    ingredient,
                    item.getQuantity().doubleValue(),
                    0.0, // Quantity after deletion is 0
                    "INVENTORY_DELETED",
                    "Inventory item deleted"
            );
        }

        // Delete the inventory item
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> findByProductId(Long productId) {
        return repo.findByProductId(productId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> findByIngredientId(Long ingredientId) {
        return repo.findByIngredientId(ingredientId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> findByWarehouseId(Long warehouseId) {
        return repo.findByWarehouseId(warehouseId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemDto> findByGroupId(Long groupId) {
        return repo.findByGroupId(groupId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
