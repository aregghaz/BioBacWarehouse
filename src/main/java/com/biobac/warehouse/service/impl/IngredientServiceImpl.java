package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.request.IngredientUpdateRequest;
import com.biobac.warehouse.request.UnitTypeConfigRequest;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.UnitTypeConfigResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.ProductHistoryService;
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
    private final UnitTypeRepository unitTypeRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;

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
            ingredient.setIngredientGroup(ingredientGroup);
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));

            for (RecipeComponent component : recipeItem.getComponents()) {
                double multiplier = request.getQuantity() != null ? request.getQuantity() : 1.0;
                double componentBaseQty = component.getQuantity() != null ? component.getQuantity() : 0.0;
                double requiredQuantity = componentBaseQty * multiplier;

                Ingredient compIng = component.getIngredient();
                Product compProd = component.getProduct();
                if (requiredQuantity > 0) {
                    if (compIng != null && compProd == null) {
                        consumeIngredientRecursive(compIng, requiredQuantity, new HashSet<>(), new HashSet<>());
                    } else if (compProd != null && compIng == null) {
                        consumeProductRecursive(compProd, requiredQuantity, new HashSet<>(), new HashSet<>());
                    } else {
                        throw new InvalidDataException("Recipe component must be either ingredient or product");
                    }
                }
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
            ingredient.setUnit(unit);
            inventoryItem.setUnit(unit);
        }

        // Handle unit type configurations on create
        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = ingredient.getUnit() != null && ingredient.getUnit().getUnitTypes() != null
                    ? ingredient.getUnit().getUnitTypes() : new HashSet<>();
            ingredient.getUnitTypeConfigs().clear();
            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));
                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                IngredientUnitType link = new IngredientUnitType();
                link.setIngredient(ingredient);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                ingredient.getUnitTypeConfigs().add(link);
            }
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
    public IngredientResponse update(Long id, IngredientUpdateRequest request) {
        Ingredient existing = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        existing.setActive(request.isActive());

        boolean inventoryNeedsUpdate = false;
        List<InventoryItem> items = existing.getInventoryItems();
        LocalDateTime now = LocalDateTime.now();

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            existing.setIngredientGroup(ingredientGroup);
            if (items != null) {
                for (InventoryItem item : items) {
                    item.setLastUpdated(now);
                }
            }
            inventoryNeedsUpdate = true;
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
            if (items != null) {
                for (InventoryItem item : items) {
                    item.setUnit(unit);
                    item.setLastUpdated(now);
                }
            }
            inventoryNeedsUpdate = true;
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            recipeItem.setIngredient(existing);
            existing.setRecipeItem(recipeItem);
        }

        // Handle unit type configurations on update
        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = existing.getUnit() != null && existing.getUnit().getUnitTypes() != null
                    ? existing.getUnit().getUnitTypes() : new HashSet<>();
            existing.getUnitTypeConfigs().clear();
            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));
                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                IngredientUnitType link = new IngredientUnitType();
                link.setIngredient(existing);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                existing.getUnitTypeConfigs().add(link);
            }
        }

        Ingredient saved = ingredientRepository.save(existing);
        if (inventoryNeedsUpdate && items != null && !items.isEmpty()) {
            inventoryItemRepository.saveAll(items);
        }

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
        response.setIngredientGroupId(ingredient.getIngredientGroup().getId());
        response.setIngredientGroupName(ingredient.getIngredientGroup().getName());

        if (ingredient.getRecipeItem() != null) {
            response.setRecipeItemId(ingredient.getRecipeItem().getId());
            response.setRecipeItemName(ingredient.getRecipeItem().getName());
        }

        if (ingredient.getUnit() != null) {
            response.setUnitId(ingredient.getUnit().getId());
            response.setUnitName(ingredient.getUnit().getName());
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
                    ir.setWarehouseId(item.getWarehouse().getId());
                    ir.setIngredientName(ingredient.getName());
                    ir.setWarehouseName(item.getWarehouse().getName());
                    if (item.getUnit() != null) {
                        ir.setUnitName(item.getUnit().getName());
                    }
                    ir.setLastUpdated(item.getLastUpdated());
                    return ir;
                })
                .toList();
        response.setTotalQuantity(totalQuantity);
        response.setInventoryItems(inventoryResponses);

        if (ingredient.getUnitTypeConfigs() != null) {
            List<UnitTypeConfigResponse> cfgs = ingredient.getUnitTypeConfigs().stream().map(cfg -> {
                UnitTypeConfigResponse r = new UnitTypeConfigResponse();
                r.setId(cfg.getId());
                if (cfg.getUnitType() != null) {
                    r.setUnitTypeId(cfg.getUnitType().getId());
                    r.setUnitTypeName(cfg.getUnitType().getName());
                }
                r.setSize(cfg.getSize());
                return r;
            }).toList();
            response.setUnitTypeConfigs(cfgs);
        }

        return response;
    }

    // Recursively consume required quantities of an ingredient from inventory or build via its recipe (supports ingredient and product components)
    private void consumeIngredientRecursive(Ingredient ingredient, double requiredQty, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds) {
        if (requiredQty <= 0) return;
        if (ingredient == null) {
            throw new InvalidDataException("Ingredient is null for consumption");
        }

        Long ingredientId = ingredient.getId();
        if (ingredientId != null) {
            if (visitingIngredientIds.contains(ingredientId)) {
                throw new InvalidDataException("Cyclic recipe detected for ingredient id=" + ingredientId);
            }
            visitingIngredientIds.add(ingredientId);
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
                double perUnit = subComp.getQuantity() != null ? subComp.getQuantity() : 0.0;
                double subRequired = perUnit * remaining;
                if (subRequired > 0) {
                    Ingredient subIng = subComp.getIngredient();
                    Product subProd = subComp.getProduct();
                    if (subIng != null && subProd == null) {
                        consumeIngredientRecursive(subIng, subRequired, visitingIngredientIds, visitingProductIds);
                    } else if (subProd != null && subIng == null) {
                        consumeProductRecursive(subProd, subRequired, visitingIngredientIds, visitingProductIds);
                    } else {
                        throw new InvalidDataException("Recipe component must be either ingredient or product");
                    }
                }
            }
        } finally {
            if (ingredientId != null) visitingIngredientIds.remove(ingredientId);
        }
    }

    // Recursively consume required quantities of a product from its inventory or build via its recipe
    private void consumeProductRecursive(Product product, double requiredQty, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds) {
        if (requiredQty <= 0) return;
        if (product == null) {
            throw new InvalidDataException("Product is null for consumption");
        }

        Long productId = product.getId();
        if (productId != null) {
            if (visitingProductIds.contains(productId)) {
                throw new InvalidDataException("Cyclic recipe detected for product id=" + productId);
            }
            visitingProductIds.add(productId);
        }
        try {
            // Try consuming from existing inventory of this product
            List<InventoryItem> inventory = product.getInventoryItems();
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
                // Record product decrease history when consuming a product for ingredient recipes
                productHistoryService.recordQuantityChange(product, available, available - toConsume, "DECREASE", "Consumed for recipe requirements");
                remaining -= toConsume;
            }

            if (remaining <= 0) return;

            // Not enough inventory; try to build from sub-components if product has a recipe
            RecipeItem subRecipe = product.getRecipeItem();
            if (subRecipe == null || subRecipe.getComponents() == null || subRecipe.getComponents().isEmpty()) {
                throw new NotEnoughException("Not enough product '" + product.getName() + "' to cover required quantity: " + requiredQty);
            }

            for (RecipeComponent subComp : subRecipe.getComponents()) {
                double perUnit = subComp.getQuantity() != null ? subComp.getQuantity() : 0.0;
                double subRequired = perUnit * remaining;
                if (subRequired > 0) {
                    Ingredient subIng = subComp.getIngredient();
                    Product subProd = subComp.getProduct();
                    if (subIng != null && subProd == null) {
                        consumeIngredientRecursive(subIng, subRequired, visitingIngredientIds, visitingProductIds);
                    } else if (subProd != null && subIng == null) {
                        consumeProductRecursive(subProd, subRequired, visitingIngredientIds, visitingProductIds);
                    } else {
                        throw new InvalidDataException("Recipe component must be either ingredient or product");
                    }
                }
            }
        } finally {
            if (productId != null) visitingProductIds.remove(productId);
        }
    }
}
