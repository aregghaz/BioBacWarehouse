package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.InventoryItemMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryIngredientCreateRequest;
import com.biobac.warehouse.request.InventoryProductCreateRequest;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.InventoryItemService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl implements InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public InventoryItemResponse createForProduct(InventoryProductCreateRequest request) {
        InventoryItem inventoryItem = new InventoryItem();

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("product not found"));
            inventoryItem.setProduct(product);
        }

        inventoryItem.setQuantity(request.getQuantity());
        // Optional unit handling
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            inventoryItem.setUnitId(unit.getId());
        }
        inventoryItem.setLastUpdated(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        InventoryItemResponse response = inventoryItemMapper.toSingleResponse(saved);
        if (saved.getUnitId() != null) {
            response.setUnitId(saved.getUnitId());
            unitRepository.findById(saved.getUnitId()).ifPresent(u -> response.setUnitName(u.getName()));
        }
        return response;
    }

    @Override
    @Transactional
    public InventoryItemResponse createForIngredient(InventoryIngredientCreateRequest request) {
        InventoryItem inventoryItem = new InventoryItem();

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }

        if (request.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new NotFoundException("ingredient not found"));
            inventoryItem.setIngredient(ingredient);
        }

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient Group not found"));
            inventoryItem.setIngredientGroup(ingredientGroup);
        }

        inventoryItem.setQuantity(request.getQuantity());
        // Optional unit handling
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            inventoryItem.setUnitId(unit.getId());
        }
        inventoryItem.setLastUpdated(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);
        InventoryItemResponse response = inventoryItemMapper.toSingleResponse(saved);
        if (saved.getUnitId() != null) {
            response.setUnitId(saved.getUnitId());
            unitRepository.findById(saved.getUnitId()).ifPresent(u -> response.setUnitName(u.getName()));
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryItemResponse>, PaginationMetadata> getByProductId(Long productId, Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir) {
        // Validate product exists
        productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("product", jakarta.persistence.criteria.JoinType.LEFT).get("id").in(productId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> {
                    InventoryItemResponse r = inventoryItemMapper.toSingleResponse(item);
                    if (item.getUnitId() != null) {
                        r.setUnitId(item.getUnitId());
                        unitRepository.findById(item.getUnitId()).ifPresent(u -> r.setUnitName(u.getName()));
                    }
                    return r;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                sortDir,
                sortBy,
                "inventoryItemTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryItemResponse>, PaginationMetadata> getByIngredientId(Long ingredientId, Map<String, FilterCriteria> filters,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   String sortBy,
                                                                                   String sortDir) {
        // Validate ingredient exists
        ingredientRepository.findById(ingredientId).orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredient", jakarta.persistence.criteria.JoinType.LEFT).get("id").in(ingredientId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> {
                    InventoryItemResponse r = inventoryItemMapper.toSingleResponse(item);
                    if (item.getUnitId() != null) {
                        r.setUnitId(item.getUnitId());
                        unitRepository.findById(item.getUnitId()).ifPresent(u -> r.setUnitName(u.getName()));
                    }
                    return r;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                sortDir,
                sortBy,
                "inventoryItemTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryItemResponse>, PaginationMetadata> getAll(Map<String, FilterCriteria> filters,
                                                                        Integer page,
                                                                        Integer size,
                                                                        String sortBy,
                                                                        String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters);

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> {
                    InventoryItemResponse r = inventoryItemMapper.toSingleResponse(item);
                    if (item.getUnitId() != null) {
                        r.setUnitId(item.getUnitId());
                        unitRepository.findById(item.getUnitId()).ifPresent(u -> r.setUnitName(u.getName()));
                    }
                    return r;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                sortDir,
                sortBy,
                "inventoryItemTable"
        );

        return Pair.of(content, metadata);
    }
}
