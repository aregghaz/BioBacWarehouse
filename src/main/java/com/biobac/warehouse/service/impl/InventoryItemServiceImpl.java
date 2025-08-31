package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.InventoryItemMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.InventoryIngredientCreateRequest;
import com.biobac.warehouse.request.InventoryProductCreateRequest;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.InventoryItemService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.specifications.InventoryItemSpecification;
import jakarta.persistence.criteria.JoinType;
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
public class InventoryItemServiceImpl implements InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final UnitRepository unitRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;

    @Override
    @Transactional
    public InventoryItemResponse createForProduct(InventoryProductCreateRequest request) {
        InventoryItem inventoryItem = new InventoryItem();

        Double totalBeforeProduct = null;
        Product productForHistory = null;

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new NotFoundException("product not found"));

            List<InventoryItem> existingInv = product.getInventoryItems();
            Double totalBeforeForHistory = existingInv != null ? existingInv.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum() : 0.0;
            totalBeforeProduct = totalBeforeForHistory;
            productForHistory = product;

            Double reqQty = request.getQuantity() != null ? request.getQuantity() : 1.0;
            RecipeItem recipeItem = product.getRecipeItem();
            if (recipeItem != null && recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty() && reqQty > 0) {
                for (RecipeComponent component : recipeItem.getComponents()) {
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double required = perUnit * reqQty;
                    if (required > 0) {
                        Ingredient compIng = component.getIngredient();
                        Product compProd = component.getProduct();
                        if (compIng != null && compProd == null) {
                            consumeIngredientRecursive(compIng, required, new HashSet<>(), new HashSet<>());
                        } else if (compProd != null && compIng == null) {
                            consumeProductRecursive(compProd, required, new HashSet<>(), new HashSet<>());
                        } else {
                            throw new InvalidDataException("Recipe component must be either ingredient or product");
                        }
                    }
                }
            }
            inventoryItem.setProduct(product);

            inventoryItem.setLastUpdated(inventoryItem.getLastUpdated()); // no-op to keep exact search context
        }

        inventoryItem.setQuantity(request.getQuantity());
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            inventoryItem.setUnit(unit);
        }
        inventoryItem.setLastUpdated(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        // Record history for added product quantity
        double addedProductQty = request.getQuantity() != null ? request.getQuantity() : 0.0;
        if (productForHistory != null && totalBeforeProduct != null && addedProductQty > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " to warehouse id=" + saved.getWarehouse().getId()
                    : "";
            productHistoryService.recordQuantityChange(
                    productForHistory,
                    totalBeforeProduct,
                    totalBeforeProduct + addedProductQty,
                    "INCREASE",
                    "Added new inventory item" + warehouseNote
            );
        }

        InventoryItemResponse response = inventoryItemMapper.toSingleResponse(saved);
        if (saved.getUnit() != null) {
            response.setUnitName(saved.getUnit().getName());
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

        Double totalBeforeForHistory = null;
        Ingredient ingredientForHistory = null;
        if (request.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new NotFoundException("ingredient not found"));

            // capture total before adding for history
            List<InventoryItem> existingInv = ingredient.getInventoryItems();
            totalBeforeForHistory = existingInv != null ? existingInv.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum() : 0.0;
            ingredientForHistory = ingredient;

            // If ingredient has a recipe
            double reqQty = request.getQuantity() != null ? request.getQuantity() : 1.0;
            RecipeItem recipeItem = ingredient.getRecipeItem();
            if (recipeItem != null && recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty() && reqQty > 0) {
                for (RecipeComponent component : recipeItem.getComponents()) {
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double required = perUnit * reqQty;
                    if (required > 0) {
                        Ingredient compIng = component.getIngredient();
                        Product compProd = component.getProduct();
                        if (compIng != null && compProd == null) {
                            consumeIngredientRecursive(compIng, required, new HashSet<>(), new HashSet<>());
                        } else if (compProd != null && compIng == null) {
                            consumeProductRecursive(compProd, required, new HashSet<>(), new HashSet<>());
                        } else {
                            throw new InvalidDataException("Recipe component must be either ingredient or product");
                        }
                    }
                }
            }

            inventoryItem.setIngredient(ingredient);
        }


        inventoryItem.setQuantity(request.getQuantity());
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            inventoryItem.setUnit(unit);
        }
        inventoryItem.setLastUpdated(LocalDateTime.now());

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        // Record history for added ingredient quantity
        double addedQty = request.getQuantity() != null ? request.getQuantity() : 0.0;
        if (ingredientForHistory != null && totalBeforeForHistory != null && addedQty > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " to warehouse id=" + saved.getWarehouse().getId()
                    : "";
            ingredientHistoryService.recordQuantityChange(
                    ingredientForHistory,
                    totalBeforeForHistory,
                    totalBeforeForHistory + addedQty,
                    "INCREASE",
                    "Added new inventory item" + warehouseNote
            );
        }

        InventoryItemResponse response = inventoryItemMapper.toSingleResponse(saved);
        if (saved.getUnit() != null) {
            response.setUnitName(saved.getUnit().getName());
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
                .and((root, query, cb) -> root.join("product", JoinType.LEFT).get("id").in(productId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> {
                    InventoryItemResponse r = inventoryItemMapper.toSingleResponse(item);
                    if (item.getUnit() != null) {
                        r.setUnitName(item.getUnit().getName());
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
        ingredientRepository.findById(ingredientId).orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredient", JoinType.LEFT).get("id").in(ingredientId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> {
                    InventoryItemResponse r = inventoryItemMapper.toSingleResponse(item);
                    if (item.getUnit() != null) {
                        r.setUnitName(item.getUnit().getName());
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
                    if (item.getUnit() != null) {
                        r.setUnitName(item.getUnit().getName());
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

            // Assume subRecipe produces 1 unit; scale components by 'remaining'
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
