package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final IngredientHistoryService ingredientHistoryService;

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

            for (RecipeComponent component : recipeItem.getComponents()) {
                double multiplier = request.getQuantity() != null ? request.getQuantity() : 1.0;
                double componentBaseQty = component.getQuantity() != null ? component.getQuantity() : 0.0;
                double requiredQuantity = componentBaseQty * multiplier;

                Ingredient requiredIngredient = component.getIngredient();
                consumeIngredientRecursive(requiredIngredient, requiredQuantity, new HashSet<>());
            }

            recipeItem.setIngredient(ingredient);
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
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            ingredient.setUnitId(unit.getId());
            inventoryItem.setUnitId(unit.getId());
        }
        ingredient.getInventoryItems().add(inventoryItem);
        Ingredient saved = ingredientRepository.save(ingredient);
        inventoryItemRepository.save(inventoryItem);

        double addedQty = request.getQuantity() != null ? request.getQuantity() : 0.0;
        if (addedQty > 0) {
            ingredientHistoryService.recordQuantityChange(saved, 0.0, addedQty, "INCREASE", "Initial stock added during ingredient creation");
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getById(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
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
        response.setIngredientGroupId(ingredient.getGroup().getId());
        response.setIngredientGroupName(ingredient.getGroup().getName());

        if(ingredient.getRecipeItem() != null) {
            response.setRecipeItemName(ingredient.getRecipeItem().getName());
        }

        if (ingredient.getUnitId() != null) {
            response.setUnitId(ingredient.getUnitId());
            unitRepository.findById(ingredient.getUnitId()).ifPresent(u -> response.setUnitName(u.getName()));
        }

        double totalQuantity = ingredient.getInventoryItems()
                .stream()
                .mapToDouble(InventoryItem::getQuantity)
                .sum();

        List<InventoryItemResponse> inventoryResponses = ingredient.getInventoryItems().stream()
                .map(item -> {
                    InventoryItemResponse ir = new InventoryItemResponse();
                    ir.setId(item.getId());
                    ir.setQuantity(item.getQuantity());
                    ir.setIngredientName(ingredient.getName());
                    ir.setWarehouseName(item.getWarehouse().getName());
                    if (item.getUnitId() != null) {
                        unitRepository.findById(item.getUnitId()).ifPresent(u -> ir.setUnitName(u.getName()));
                    }
                    ir.setLastUpdated(item.getLastUpdated());
                    return ir;
                })
                .toList();
        response.setTotalQuantity(totalQuantity);
        response.setInventoryItems(inventoryResponses);

        return response;
    }

    private void consumeIngredientRecursive(Ingredient ingredient, double requiredQty, Set<Long> visiting) {
        if (requiredQty <= 0) return;
        if (ingredient == null) {
            throw new InvalidDataException("Ingredient is null for consumption");
        }

        Long ingredientId = ingredient.getId();
        if (ingredientId != null) {
            if (visiting.contains(ingredientId)) {
                throw new InvalidDataException("Cyclic recipe detected for ingredient id=" + ingredientId);
            }
            visiting.add(ingredientId);
        }
        try {
            // 1) Try consuming from existing inventory of this ingredient
            List<InventoryItem> inventory = ingredient.getInventoryItems();
            double available = inventory != null ? inventory.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum() : 0.0;

            double remaining = requiredQty;
            if (available > 0) {
                double toConsume = Math.min(available, remaining);
                double left = toConsume;
                if (inventory != null) {
                    for (InventoryItem inv : inventory) {
                        if (left <= 0) break;
                        double invQty = inv.getQuantity() != null ? inv.getQuantity() : 0.0;
                        double use = Math.min(invQty, left);
                        if (use > 0) {
                            inv.setQuantity(invQty - use);
                            inv.setLastUpdated(LocalDateTime.now());
                            left -= use;
                        }
                    }
                    inventoryItemRepository.saveAll(inventory);
                }
                ingredientHistoryService.recordQuantityChange(ingredient, available, available - toConsume, "DECREASE", "Consumed for recipe requirements");
                remaining -= toConsume;
            }

            if (remaining <= 0) return;

            // 2) Not enough inventory; try to build from sub-components if recipe exists
            RecipeItem subRecipe = ingredient.getRecipeItem();
            if (subRecipe == null || subRecipe.getComponents() == null || subRecipe.getComponents().isEmpty()) {
                throw new NotEnoughException("Not enough ingredient '" + ingredient.getName() + "' to cover required quantity: " + requiredQty);
            }

            // Assume subRecipe produces 1 unit of this ingredient per recipe; so scale component needs by 'remaining'
            for (RecipeComponent subComp : subRecipe.getComponents()) {
                Ingredient subIng = subComp.getIngredient();
                double perUnit = subComp.getQuantity() != null ? subComp.getQuantity() : 0.0;
                double subRequired = perUnit * remaining;
                if (subRequired > 0) {
                    consumeIngredientRecursive(subIng, subRequired, visiting);
                }
            }
        } finally {
            if (ingredientId != null) visiting.remove(ingredientId);
        }
    }
}
