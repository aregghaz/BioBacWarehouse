package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.response.InventoryItemResponse;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.specifications.ProductSpecification;
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
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final UnitRepository unitRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setCompanyId(request.getCompanyId());

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));

            double multiplier = request.getQuantity() != null ? request.getQuantity() : 1.0;
            if (recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty() && multiplier > 0) {
                for (RecipeComponent component : recipeItem.getComponents()) {
                    Ingredient ingredient = component.getIngredient();
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double requiredQty = perUnit * multiplier;
                    if (requiredQty > 0) {
                        consumeIngredientRecursive(ingredient, requiredQty, new HashSet<>());
                    }
                }
            }
            recipeItem.setProduct(product);
            product.setRecipeItem(recipeItem);
        }

        InventoryItem inventoryItem = new InventoryItem();
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            inventoryItem.setWarehouse(warehouse);
        }
        inventoryItem.setQuantity(request.getQuantity());
        inventoryItem.setProduct(product);
        inventoryItem.setLastUpdated(LocalDateTime.now());
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            product.setUnit(unit);
            inventoryItem.setUnit(unit);
        }
        product.getInventoryItems().add(inventoryItem);
        Product saved = productRepository.save(product);
        inventoryItemRepository.save(inventoryItem);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (request.getName() != null) {
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getSku() != null) {
            existing.setSku(request.getSku());
        }
        if (request.getCompanyId() != null) {
            existing.setCompanyId(request.getCompanyId());
        }

        boolean inventoryNeedsUpdate = false;
        List<InventoryItem> items = existing.getInventoryItems();
        LocalDateTime now = LocalDateTime.now();

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
            recipeItem.setProduct(existing);
            existing.setRecipeItem(recipeItem);
        }

        Product saved = productRepository.save(existing);
        if (inventoryNeedsUpdate && items != null && !items.isEmpty()) {
            inventoryItemRepository.saveAll(items);
        }
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Product> spec = ProductSpecification.buildSpecification(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "productTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new NotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setSku(product.getSku());

        if (product.getRecipeItem() != null) {
            response.setRecipeItemName(product.getRecipeItem().getName());
            response.setRecipeItemId(product.getRecipeItem().getId());
        }

        double totalQuantity = product.getInventoryItems()
                .stream()
                .mapToDouble(InventoryItem::getQuantity)
                .sum();

        if (product.getUnit() != null) {
            response.setUnitId(product.getUnit().getId());
            response.setUnitName(product.getUnit().getName());
        }

        List<InventoryItemResponse> inventoryResponses = product.getInventoryItems().stream()
                .map(item -> {
                    InventoryItemResponse ir = new InventoryItemResponse();
                    ir.setId(item.getId());
                    ir.setQuantity(item.getQuantity());
                    ir.setProductName(product.getName());
                    ir.setWarehouseName(item.getWarehouse().getName());
                    ir.setWarehouseId(item.getWarehouse().getId());
                    if (item.getUnit() != null) {
                        ir.setUnitName(item.getUnit().getName());
                    }
                    ir.setLastUpdated(item.getLastUpdated());
                    return ir;
                })
                .toList();
        response.setTotalQuantity(totalQuantity);
        response.setInventoryItems(inventoryResponses);

        return response;
    }

    // Recursively consume required quantities of an ingredient either from inventory or, if insufficient, from sub-components via its recipe
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
            // Try consuming from existing inventory of this ingredient
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

            // Not enough inventory; try to build from sub-components if ingredient has a recipe
            RecipeItem subRecipe = ingredient.getRecipeItem();
            if (subRecipe == null || subRecipe.getComponents() == null || subRecipe.getComponents().isEmpty()) {
                throw new NotEnoughException("Not enough ingredient '" + ingredient.getName() + "' to cover required quantity: " + requiredQty);
            }

            // Assume subRecipe produces 1 unit per cycle; scale components by 'remaining'
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