package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.IngredientResponse;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final IngredientMapper ingredientMapper;
    private final AttributeClient attributeClient;

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
        ingredient.setDescription(request.getDescription());
        ingredient.setPrice(request.getPrice());
        if (request.getExpiration() != null) {
            ingredient.setExpiration(request.getExpiration());
        }

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            ingredient.setIngredientGroup(ingredientGroup);
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

        if (ingredient.getUnit() != null) {
            Unit unit = ingredient.getUnit();

            UnitType baseUnitType = unitTypeRepository.findByName(unit.getName())
                    .orElseGet(() -> {
                        UnitType newType = new UnitType();
                        newType.setName(unit.getName());
                        return unitTypeRepository.save(newType);
                    });

            boolean alreadyExists = ingredient.getUnitTypeConfigs().stream()
                    .anyMatch(link -> link.getUnitType().equals(baseUnitType));

            if (!alreadyExists) {
                IngredientUnitType baseLink = new IngredientUnitType();
                baseLink.setIngredient(ingredient);
                baseLink.setUnitType(baseUnitType);
                baseLink.setSize(1.0);
                ingredient.getUnitTypeConfigs().add(baseLink);
            }
        }

        Ingredient saved = ingredientRepository.save(ingredient);

        ingredientHistoryService.recordQuantityChange(saved, 0.0, 0.0, "INCREASE", "Added new ingredient to system");


        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeClient.createValues(saved.getId(), AttributeTargetType.INGREDIENT.name(), request.getAttributes());

        }

        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            saved.setAttributeGroupIds(request.getAttributeGroupIds());
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

        if (request.getPrice() != null) {
            existing.setPrice(request.getPrice());
        }

        if (request.getExpiration() != null) {
            existing.setExpiration(request.getExpiration());
        }

        if (request.getIngredientGroupId() != null) {
            IngredientGroup ingredientGroup = ingredientGroupRepository.findById(request.getIngredientGroupId())
                    .orElseThrow(() -> new NotFoundException("Ingredient group not found"));
            existing.setIngredientGroup(ingredientGroup);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
        }


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

        if (request.getAttributeGroupIds() != null) {
            existing.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        Ingredient saved = ingredientRepository.save(existing);

        List<AttributeUpsertRequest> attributes = request.getAttributeGroupIds() == null || request.getAttributeGroupIds().isEmpty() ? Collections.emptyList() : request.getAttributes();

        attributeClient.updateValues(saved.getId(), AttributeTargetType.INGREDIENT.name(), request.getAttributeGroupIds(), attributes);

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

        attributeClient.deleteValues(id, AttributeTargetType.INGREDIENT.name());

        double totalBefore = 0.0;
        List<InventoryItem> beforeItems = ingredient.getInventoryItems();
        if (beforeItems != null) {
            totalBefore = beforeItems.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum();
        }


//        List<RecipeComponent> refComponents = ingredient.getRecipeComponents();
//        if (refComponents != null && !refComponents.isEmpty()) {
//            for (RecipeComponent rc : refComponents) {
//                rc.setIngredient(null);
//            }
//        }

        List<InventoryItem> items = ingredient.getInventoryItems();
        if (items != null && !items.isEmpty()) {
            inventoryItemRepository.deleteAll(items);
            ingredient.getInventoryItems().clear();
        }

        ingredient.setDeleted(true);
        ingredientRepository.save(ingredient);

        ingredientHistoryService.recordQuantityChange(ingredient, totalBefore, 0.0, "DELETE", "Soft deleted");
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getAllExcludeRecipeIngredient(Long recipeItemId) {
//        List<Ingredient> ingredients = ingredientRepository.findAllByDeletedFalseExcludeRecipe(recipeItemId);
//
//        return ingredients.stream()
//                .map(ingredientMapper::toResponse)
//                .toList();
        return null;
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

            if (remaining > 0) {
                throw new NotEnoughException("Not enough ingredient '" + ingredient.getName() + "' to cover required quantity: " + requiredQty);
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
