package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.utils.specifications.IngredientSpecification;
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
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public IngredientResponse create(IngredientCreateRequest request) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(request.getName());
        ingredient.setActive(request.isActive());
        ingredient.setDescription(request.getDescription());
        InventoryItem inventoryItem = new InventoryItem();

        if (request.getGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            ingredient.setGroup(ingredientGroup);
            inventoryItem.setIngredientGroup(ingredientGroup);
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            ingredient.setRecipeItem(recipeItem);
        }

        inventoryItem.setQuantity(request.getQuantity());
        inventoryItem.setIngredient(ingredient);
        inventoryItem.setLastUpdated(LocalDateTime.now());
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }
        // Unit handling: if provided, validate and set unitId fields
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            ingredient.setUnitId(unit.getId());
            inventoryItem.setUnitId(unit.getId());
        }
        ingredient.getInventoryItems().add(inventoryItem);
        Ingredient saved = ingredientRepository.save(ingredient);
        inventoryItemRepository.save(inventoryItem);

        return toResponse(saved);
    }

    @Override
    public IngredientResponse getById(Long id) {
        Ingredient ingredient =  ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        return toResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {

        List<Ingredient> ingredients = ingredientRepository.findAll();

        return ingredients.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public IngredientResponse update(Long id, Ingredient payload) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        if (payload.getName() != null) existing.setName(payload.getName());
        if (payload.getDescription() != null) existing.setDescription(payload.getDescription());

        // Update group if provided
        if (payload.getGroup() != null && payload.getGroup().getId() != null) {
            IngredientGroup group = ingredientGroupRepository.findById(payload.getGroup().getId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            existing.setGroup(group);
        }

        // Unit update: only if provided
        if (payload.getUnitId() != null) {
            // validate id exists
            Unit unit = unitRepository.findById(payload.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnitId(unit.getId());
        }

        Ingredient saved = ingredientRepository.save(existing);
        return toResponse(saved);

    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sortBy,
                                                                            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Ingredient> spec = IngredientSpecification.buildSpecification(filters);
        Page<Ingredient> ingredientPage = ingredientRepository.findAll(spec, pageable);

        List<IngredientResponse> content = ingredientPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientPage.getNumber(),
                ingredientPage.getSize(),
                ingredientPage.getTotalElements(),
                ingredientPage.getTotalPages(),
                ingredientPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "ingredientTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!ingredientRepository.existsById(id)) {
            throw new NotFoundException("Ingredient not found");
        }
        ingredientRepository.deleteById(id);
    }

    private IngredientResponse toResponse(Ingredient ingredient) {
        IngredientResponse response = new IngredientResponse();
        response.setId(ingredient.getId());
        response.setName(ingredient.getName());
        response.setDescription(ingredient.getDescription());
        response.setActive(ingredient.isActive());

        if (ingredient.getGroup() != null) {
            response.setGroupId(ingredient.getGroup().getId());
        }
        // Set unit info for ingredient (preferred unit)
        if (ingredient.getUnitId() != null) {
            response.setUnitId(ingredient.getUnitId());
            unitRepository.findById(ingredient.getUnitId()).ifPresent(u -> response.setUnitName(u.getName()));
        }

        List<InventoryItemResponse> inventoryResponses = ingredient.getInventoryItems().stream()
                .map(item -> {
                    InventoryItemResponse ir = new InventoryItemResponse();
                    ir.setId(item.getId());
                    ir.setQuantity(item.getQuantity());
                    // Set unit info for inventory item
                    if (item.getUnitId() != null) {
                        ir.setUnitId(item.getUnitId());
                        unitRepository.findById(item.getUnitId()).ifPresent(u -> ir.setUnitName(u.getName()));
                    }
                    ir.setLastUpdated(item.getLastUpdated());
//                    if (item.getWarehouse() != null) {
//                        ir.setWarehouseId(item.getWarehouse().getId());
//                    }
                    return ir;
                })
                .toList();

        response.setInventoryItems(inventoryResponses);

        return response;
    }
}
