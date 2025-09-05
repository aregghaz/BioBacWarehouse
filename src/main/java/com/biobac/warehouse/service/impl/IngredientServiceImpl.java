package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.IngredientCreateRequest;
import com.biobac.warehouse.request.IngredientUpdateRequest;
import com.biobac.warehouse.request.UnitTypeConfigRequest;
import com.biobac.warehouse.response.IngredientResponse;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.IngredientService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.specifications.IngredientSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
    private final IngredientMapper ingredientMapper;
    private final AttributeService attributeService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size <= 0 ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        String sd = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir;
        Sort sort = sd.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        if (safeSize > 1000) {
            log.warn("Requested page size {} is too large, capping to 1000", safeSize);
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

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

        if (request.getCompanyId() != null) {
            inventoryItem.setCompanyId(request.getCompanyId());
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
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            ingredient.setUnit(unit);
        }

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

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeService.createValuesForIngredient(saved, request.getAttributes());
        }

        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getById(Long id) {
        Ingredient ingredient = ingredientRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        return ingredientMapper.toResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAll() {

        List<Ingredient> ingredients = ingredientRepository.findAllByDeletedFalse();

        return ingredients.stream()
                .map(ingredientMapper::toResponse)
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

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            existing.setIngredientGroup(ingredientGroup);
            inventoryNeedsUpdate = true;
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
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

        return ingredientMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sortBy,
                                                                            String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Ingredient> spec = IngredientSpecification.buildSpecification(filters);
        Page<Ingredient> ingredientPage = ingredientRepository.findAll(spec, pageable);

        List<IngredientResponse> content = ingredientPage.getContent()
                .stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientPage.getNumber(),
                ingredientPage.getSize(),
                ingredientPage.getTotalElements(),
                ingredientPage.getTotalPages(),
                ingredientPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "ingredientTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        double totalBefore = 0.0;
        List<InventoryItem> beforeItems = ingredient.getInventoryItems();
        if (beforeItems != null) {
            totalBefore = beforeItems.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum();
        }

        RecipeItem recipeItem = ingredient.getRecipeItem();
        if (recipeItem != null) {
            recipeItem.setIngredient(null);
            recipeItemRepository.save(recipeItem);
            ingredient.setRecipeItem(null);
        }

        List<RecipeComponent> refComponents = ingredient.getRecipeComponents();
        if (refComponents != null && !refComponents.isEmpty()) {
            for (RecipeComponent rc : refComponents) {
                rc.setIngredient(null);
            }
        }

        List<InventoryItem> items = ingredient.getInventoryItems();
        if (items != null && !items.isEmpty()) {
            inventoryItemRepository.deleteAll(items);
            ingredient.getInventoryItems().clear();
        }

        ingredient.setActive(false);
        ingredient.setDeleted(true);
        ingredientRepository.save(ingredient);

        ingredientHistoryService.recordQuantityChange(ingredient, totalBefore, 0.0, "DELETE", "Soft deleted");
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
