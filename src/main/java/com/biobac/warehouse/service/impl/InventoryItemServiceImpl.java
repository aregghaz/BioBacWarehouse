package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.InventoryItemMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.ComponentInventorySelection;
import com.biobac.warehouse.request.ExtraInventorySelection;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ApiResponse;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryItemServiceImpl implements InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemMapper inventoryItemMapper;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final CompanyClient companyClient;
    private final ComponentBalanceRepository componentBalanceRepository;






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
                .map(item -> enrichCompany(item, inventoryItemMapper.toSingleResponse(item)))
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
    public Pair<List<InventoryItemResponse>, PaginationMetadata> getByWarehouseId(Long warehouseId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        warehouseRepository.findById(warehouseId).orElseThrow(() -> new NotFoundException("Warehouse not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("warehouse", JoinType.LEFT).get("id").in(warehouseId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> enrichCompany(item, inventoryItemMapper.toSingleResponse(item)))
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

    private void consumeIngredientRecursive(Ingredient ingredient, double requiredQty, SelectionContext selectionContext,
                                            Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason) {
        if (requiredQty <= 0) return;
        if (ingredient == null) {
            throw new InvalidDataException("Ingredient is null for consumption");
        }

        Long ingredientId = ingredient.getId();
        if (ingredientId != null) {
            if (visitingIngredientIds.contains(ingredientId)) {
                throw new InvalidDataException("Cyclic recipe detected for ingredient " + ingredient.getName());
            }
            visitingIngredientIds.add(ingredientId);
        }
        try {
            Warehouse defWh = ingredient.getDefaultWarehouse();
            if (defWh == null || defWh.getId() == null) {
                throw new InvalidDataException("Default warehouse is not set for ingredient " + ingredient.getName());
            }
            ComponentBalance cb = getOrCreateIngredientBalance(defWh, ingredient);
            double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
            double after = before - requiredQty;
            cb.setBalance(after);
            componentBalanceRepository.save(cb);

            String where = " from warehouse " + defWh.getName();
            ingredientHistoryService.recordQuantityChange(
                    ingredient,
                    before,
                    after,
                    "DECREASE",
                    (reason != null ? reason : "Consumed for recipe requirements") + where
            );
        } finally {
            if (ingredientId != null) visitingIngredientIds.remove(ingredientId);
        }
    }

    private InventoryItemResponse enrichCompany(InventoryItem item, InventoryItemResponse resp) {
        if (item == null || resp == null) return resp;
        Long cid = item.getCompanyId();
        resp.setCompanyId(cid);
        if (cid != null) {
            try {
                ApiResponse<String> api = companyClient.getCompanyName(cid);
                if (api != null && Boolean.TRUE.equals(api.getSuccess())) {
                    resp.setCompanyName(api.getData());
                } else if (api != null && api.getData() != null) {
                    resp.setCompanyName(api.getData());
                }
            } catch (Exception ignored) {
            }
        }
        return resp;
    }

    private ComponentBalance getOrCreateIngredientBalance(Warehouse warehouse, Ingredient ingredient) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (ingredient)");
        }
        if (ingredient == null || ingredient.getId() == null) {
            throw new InvalidDataException("Ingredient is required for component balance");
        }
        return componentBalanceRepository.findByWarehouseIdAndIngredientId(warehouse.getId(), ingredient.getId())
                .orElseGet(() -> {
                    ComponentBalance cb = new ComponentBalance();
                    cb.setWarehouse(warehouse);
                    cb.setIngredient(ingredient);
                    cb.setBalance(0.0);
                    return componentBalanceRepository.save(cb);
                });
    }

    private ComponentBalance getOrCreateProductBalance(Warehouse warehouse, Product product) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (product)");
        }
        if (product == null || product.getId() == null) {
            throw new InvalidDataException("Product is required for component balance");
        }
        return componentBalanceRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseGet(() -> {
                    ComponentBalance cb = new ComponentBalance();
                    cb.setWarehouse(warehouse);
                    cb.setProduct(product);
                    cb.setBalance(0.0);
                    return componentBalanceRepository.save(cb);
                });
    }

    private void increaseBalanceForIngredient(Warehouse warehouse, Ingredient ingredient, double qty) {
        if (qty == 0) return;
        ComponentBalance cb = getOrCreateIngredientBalance(warehouse, ingredient);
        double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
        double after = before + qty;
        cb.setBalance(after);
        componentBalanceRepository.save(cb);
    }

    private void increaseBalanceForProduct(Warehouse warehouse, Product product, double qty) {
        if (qty == 0) return;
        ComponentBalance cb = getOrCreateProductBalance(warehouse, product);
        double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
        double after = before + qty;
        cb.setBalance(after);
        componentBalanceRepository.save(cb);
    }

    private void consumeProductRecursive(Product product, double requiredQty, SelectionContext selectionContext,
                                         Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason) {
        if (requiredQty <= 0) return;
        if (product == null) {
            throw new InvalidDataException("Product is null for consumption");
        }

        Long productId = product.getId();
        if (productId != null) {
            if (visitingProductIds.contains(productId)) {
                throw new InvalidDataException("Cyclic recipe detected for product " + product.getName());
            }
            visitingProductIds.add(productId);
        }
        try {
            Warehouse defWh = product.getDefaultWarehouse();
            if (defWh == null || defWh.getId() == null) {
                throw new InvalidDataException("Default warehouse is not set for product " + product.getName());
            }
            ComponentBalance cb = getOrCreateProductBalance(defWh, product);
            double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
            double after = before - requiredQty;
            cb.setBalance(after);
            componentBalanceRepository.save(cb);

            String where = " from warehouse " + defWh.getName();
            productHistoryService.recordQuantityChange(
                    product,
                    before,
                    after,
                    "DECREASE",
                    (reason != null ? reason : "Consumed for recipe requirements") + where
            );
        } finally {
            if (productId != null) visitingProductIds.remove(productId);
        }
    }

    private void consumeExtraComponents(List<ProductComponent> components, double totalCount) {
        if (components == null || components.isEmpty()) return;
        SelectionContext sc = null;

        for (ProductComponent pc : components) {
            Ingredient compIng = pc.getIngredient();
            Product compProd = pc.getChildProduct();
            if ((compIng == null && compProd == null) || (compIng != null && compProd != null)) {
                throw new InvalidDataException("Extra component must be either ingredient or product");
            }

            double perUnit = pc.getQuantity() != null ? pc.getQuantity() : 0.0;
            double required = perUnit * totalCount;
            if (required <= 0) {
                continue;
            }

            if (compIng != null) {
                consumeIngredientRecursive(compIng, required, sc, new HashSet<>(), new HashSet<>(), "Consumed for extra components");
            } else {
                consumeProductRecursive(compProd, required, sc, new HashSet<>(), new HashSet<>(), "Consumed for extra components");
            }
        }
    }

    private SelectionContext buildSelectionContextFromExtra(List<ExtraInventorySelection> selections) {
        if (selections == null || selections.isEmpty()) {
            return new SelectionContext(Map.of(), Map.of());
        }
        Map<Long, Long> ing = selections.stream()
                .filter(s -> s.getIngredientId() != null && s.getInventoryItemId() != null)
                .collect(Collectors.toMap(ExtraInventorySelection::getIngredientId,
                        ExtraInventorySelection::getInventoryItemId,
                        (a, b) -> {
                            if (!a.equals(b)) {
                                throw new InvalidDataException("Duplicate selections for the same ingredient with different inventoryItemId");
                            }
                            return a;
                        }));
        Map<Long, Long> prod = selections.stream()
                .filter(s -> s.getProductId() != null && s.getInventoryItemId() != null)
                .collect(Collectors.toMap(ExtraInventorySelection::getProductId,
                        ExtraInventorySelection::getInventoryItemId,
                        (a, b) -> {
                            if (!a.equals(b)) {
                                throw new InvalidDataException("Duplicate selections for the same product with different inventoryItemId");
                            }
                            return a;
                        }));
        return new SelectionContext(ing, prod);
    }

    private void consumeRecipeItem(double totalCount, RecipeItem recipeItem) {
        if (totalCount > 0) {
            if (recipeItem != null && recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
                SelectionContext selectionContext = null;
                for (RecipeComponent component : recipeItem.getComponents()) {
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double required = perUnit * totalCount;
                    if (required <= 0) continue;

                    Ingredient compIng = component.getIngredient();
                    Product compProd = component.getProduct();
                    if (compIng != null && compProd == null) {
                        consumeIngredientRecursive(compIng, required, selectionContext, new HashSet<>(), new HashSet<>(), "Consumed for recipe requirements");
                    } else if (compProd != null && compIng == null) {
                        consumeProductRecursive(compProd, required, selectionContext, new HashSet<>(), new HashSet<>(), "Consumed for recipe requirements");
                    } else {
                        throw new InvalidDataException("Recipe component must be either ingredient or product");
                    }
                }
            }
        }
    }

    private record SelectionContext(Map<Long, Long> ingredientMap, Map<Long, Long> productMap) {
        private SelectionContext(Map<Long, Long> ingredientMap, Map<Long, Long> productMap) {
            this.ingredientMap = ingredientMap != null ? ingredientMap : Map.of();
            this.productMap = productMap != null ? productMap : Map.of();
        }

        boolean hasIngredientSelection(Long id) {
            return id != null && ingredientMap.containsKey(id);
        }

        boolean hasProductSelection(Long id) {
            return id != null && productMap.containsKey(id);
        }

        Long getIngredientSelectedId(Long id) {
            return ingredientMap.get(id);
        }

        Long getProductSelectedId(Long id) {
            return productMap.get(id);
        }
    }

    private SelectionContext buildSelectionContext(List<ComponentInventorySelection> selections) {
        if (selections == null || selections.isEmpty()) {
            return new SelectionContext(Map.of(), Map.of());
        }
        Map<Long, Long> ing = selections.stream()
                .filter(s -> s.getIngredientId() != null && s.getInventoryItemId() != null)
                .collect(Collectors.toMap(ComponentInventorySelection::getIngredientId,
                        ComponentInventorySelection::getInventoryItemId,
                        (a, b) -> {
                            if (!a.equals(b)) {
                                throw new InvalidDataException("Duplicate selections for the same ingredient with different inventoryItemId");
                            }
                            return a; // same value
                        }));
        Map<Long, Long> prod = selections.stream()
                .filter(s -> s.getProductId() != null && s.getInventoryItemId() != null)
                .collect(Collectors.toMap(ComponentInventorySelection::getProductId,
                        ComponentInventorySelection::getInventoryItemId,
                        (a, b) -> {
                            if (!a.equals(b)) {
                                throw new InvalidDataException("Duplicate selections for the same product with different inventoryItemId");
                            }
                            return a; // same value
                        }));
        return new SelectionContext(ing, prod);
    }
}