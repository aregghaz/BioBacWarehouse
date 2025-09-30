package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.CompanyClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.InventoryItemMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
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
    private final IngredientUnitTypeRepository ingredientUnitTypeRepository;

    @Override
    @Transactional
    public InventoryItemResponse createForProduct(InventoryProductCreateRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        if (product.getExtraComponents() != null && !product.getExtraComponents().isEmpty()) {
            consumeExtraComponents(product.getExtraComponents(), request.getExtraInventorySelections());
        }

        if (product.getRecipeItem() != null) {
            consumeRecipeItem(totalCount, product.getRecipeItem(), request.getRecipeInventorySelections());
        }

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setProduct(product);
        inventoryItem.setPrice(request.getPrice());
        inventoryItem.setManufacturingDate(request.getManufacturingDate());
        inventoryItem.setExpirationDate(request.getManufacturingDate().plusDays(product.getExpiration()));
        inventoryItem.setQuantity(totalCount);

        double totalBefore = product.getInventoryItems() != null
                ? product.getInventoryItems().stream().mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0).sum()
                : 0.0;

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        if (totalCount > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " to warehouse id=" + saved.getWarehouse().getId() : "";
            productHistoryService.recordQuantityChange(
                    product,
                    totalBefore,
                    totalBefore + totalCount,
                    "INCREASE",
                    "Added new inventory item" + warehouseNote
            );
        }

        return inventoryItemMapper.toSingleResponse(saved);
    }

    @Override
    @Transactional
    public InventoryItemResponse createForIngredient(InventoryIngredientCreateRequest request) {
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setWarehouse(warehouse);
        inventoryItem.setIngredient(ingredient);
        inventoryItem.setCompanyId(request.getCompanyId());
        inventoryItem.setPrice(request.getPrice());
        inventoryItem.setImportDate(request.getImportDate());
        inventoryItem.setManufacturingDate(request.getManufacturingDate());
        inventoryItem.setExpirationDate(request.getManufacturingDate().plusDays(ingredient.getExpiration()));
        inventoryItem.setQuantity(totalCount);

        double totalBefore = ingredient.getInventoryItems() != null
                ? ingredient.getInventoryItems().stream().mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0).sum()
                : 0.0;

        InventoryItem saved = inventoryItemRepository.save(inventoryItem);

        if (totalCount > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " to warehouse id=" + saved.getWarehouse().getId() : "";
            ingredientHistoryService.recordQuantityChange(
                    ingredient,
                    totalBefore,
                    totalBefore + totalCount,
                    "INCREASE",
                    "Added new inventory item" + warehouseNote,
                    request.getPrice(),
                    request.getCompanyId()
            );
        }

        return enrichCompany(saved, inventoryItemMapper.toSingleResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<InventoryItemResponse>, PaginationMetadata> getByProductId(Long productId, Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir) {
        productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<InventoryItem> spec = InventoryItemSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("product", JoinType.LEFT).get("id").in(productId));

        Page<InventoryItem> pageResult = inventoryItemRepository.findAll(spec, pageable);

        List<InventoryItemResponse> content = pageResult.getContent()
                .stream()
                .map(item -> enrichCompany(item, inventoryItemMapper.toSingleResponse(item)))
                .collect(Collectors.toList());

        PaginationMetadata metadata = PaginationMetadata.builder()
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .filter(filters)
                .sortDir(sortDir)
                .sortBy(sortBy)
                .table("inventoryItemTable")
                .build();

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
    public Map<Long, List<InventoryItemResponse>> getAllByIngredientIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByIngredientIdIn(ids);

        return inventoryItems.stream()
                .map(inventoryItemMapper::toSingleResponse)
                .collect(Collectors.groupingBy(InventoryItemResponse::getIngredientId));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<InventoryItemResponse>> getAllByProductIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        List<InventoryItem> inventoryItems = inventoryItemRepository.findByProductIdIn(ids);

        return inventoryItems.stream()
                .map(inventoryItemMapper::toSingleResponse)
                .collect(Collectors.groupingBy(InventoryItemResponse::getProductId));
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
                throw new InvalidDataException("Cyclic recipe detected for ingredient id=" + ingredientId);
            }
            visitingIngredientIds.add(ingredientId);
        }
        try {
            List<InventoryItem> allInventory = ingredient.getInventoryItems();
            double totalBeforeAll = allInventory != null ? allInventory.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0).sum() : 0.0;

            // Determine a single inventory item to consume from
            Long selectedInventoryItemId = null;
            if (selectionContext != null && selectionContext.hasIngredientSelection(ingredientId)) {
                selectedInventoryItemId = selectionContext.getIngredientSelectedId(ingredientId);
                if (selectedInventoryItemId == null) {
                    throw new InvalidDataException("No inventory item selected for ingredient id=" + ingredientId);
                }
            }

            InventoryItem target = null;
            if (allInventory != null && !allInventory.isEmpty()) {
                if (selectedInventoryItemId != null) {
                    for (InventoryItem inv : allInventory) {
                        if (inv.getId() != null && inv.getId().equals(selectedInventoryItemId)) {
                            target = inv;
                            break;
                        }
                    }
                    if (target == null) {
                        throw new NotFoundException("Selected inventory item id=" + selectedInventoryItemId + " not found for ingredient id=" + ingredientId);
                    }
                } else {
                    throw new InvalidDataException("Inventory items not found");
                }
            }

            if (target != null) {
                double invQty = target.getQuantity() != null ? target.getQuantity() : 0.0;
                target.setQuantity(invQty - requiredQty);
                inventoryItemRepository.save(target);

                String where = " using inventory item id=" + selectedInventoryItemId;
                ingredientHistoryService.recordQuantityChange(ingredient, totalBeforeAll, totalBeforeAll - requiredQty, "DECREASE",
                        (reason != null ? reason : "Consumed for recipe requirements") + where, null, null);
                return;
            }

            String where = selectedInventoryItemId != null ? " in selected inventory item id=" + selectedInventoryItemId : " in any single inventory item";
            throw new NotEnoughException("Not enough ingredient '" + ingredient.getName() + "' to cover required quantity: " + requiredQty + where);
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

    private void consumeProductRecursive(Product product, double requiredQty, SelectionContext selectionContext,
                                         Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason) {
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
            List<InventoryItem> allInventory = product.getInventoryItems();
            double totalBeforeAll = allInventory != null ? allInventory.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0).sum() : 0.0;

            Long selectedInventoryItemId = null;
            if (selectionContext != null && selectionContext.hasProductSelection(productId)) {
                selectedInventoryItemId = selectionContext.getProductSelectedId(productId);
                if (selectedInventoryItemId == null) {
                    throw new InvalidDataException("No inventory item selected for product id=" + productId);
                }
            }

            InventoryItem target = null;
            if (allInventory != null && !allInventory.isEmpty()) {
                if (selectedInventoryItemId != null) {
                    for (InventoryItem inv : allInventory) {
                        if (inv.getId() != null && inv.getId().equals(selectedInventoryItemId)) {
                            target = inv;
                            break;
                        }
                    }
                    if (target == null) {
                        throw new NotFoundException("Selected inventory item id=" + selectedInventoryItemId + " not found for product id=" + productId);
                    }
                } else {
                    throw new InvalidDataException("Inventory items not found");
                }
            }

            if (target != null) {
                double invQty = target.getQuantity() != null ? target.getQuantity() : 0.0;
                target.setQuantity(invQty - requiredQty);
                inventoryItemRepository.save(target);

                String where = " using inventory item id=" + selectedInventoryItemId;
                productHistoryService.recordQuantityChange(product, totalBeforeAll, totalBeforeAll - requiredQty, "DECREASE",
                        (reason != null ? reason : "Consumed for recipe requirements") + where);
                return;
            }

            String where = selectedInventoryItemId != null ? " in selected inventory item id=" + selectedInventoryItemId : " in any single inventory item";
            throw new NotEnoughException("Not enough product '" + product.getName() + "' to cover required quantity: " + requiredQty + where);
        } finally {
            if (productId != null) visitingProductIds.remove(productId);
        }
    }

    private void consumeExtraComponents(List<ProductComponent> components, List<ExtraInventorySelection> selections) {
        if (components == null || components.isEmpty()) return;
        if (selections == null || selections.isEmpty()) {
            throw new InvalidDataException("Extra inventory selections are required to consume extra components");
        }

        Map<Long, ExtraInventorySelection> byIngredient = new HashMap<>();
        Map<Long, ExtraInventorySelection> byProduct = new HashMap<>();
        for (ExtraInventorySelection s : selections) {
            if (s.getIngredientId() != null) byIngredient.put(s.getIngredientId(), s);
            if (s.getProductId() != null) byProduct.put(s.getProductId(), s);
        }

        for (ProductComponent pc : components) {
            Ingredient compIng = pc.getIngredient();
            Product compProd = pc.getChildProduct();
            if ((compIng == null && compProd == null) || (compIng != null && compProd != null)) {
                throw new InvalidDataException("Extra component must be either ingredient or product");
            }

            ExtraInventorySelection sel;
            if (compIng != null) {
                Long id = compIng.getId();
                sel = id != null ? byIngredient.get(id) : null;
            } else {
                Long id = compProd.getId();
                sel = id != null ? byProduct.get(id) : null;
            }

            if (sel == null) {
                throw new InvalidDataException("Missing selection for extra component");
            }
            if (sel.getInventoryItemId() == null) {
                throw new InvalidDataException("Inventory item selection is required for extra component");
            }
            double required = sel.getQuantity() != null ? sel.getQuantity() : 0.0;
            if (required <= 0) {
                continue;
            }

            SelectionContext sc = buildSelectionContextFromExtra(List.of(sel));

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

    private void consumeRecipeItem(double totalCount, RecipeItem recipeItem, List<ComponentInventorySelection> recipeInventorySelections) {
        if (totalCount > 0) {
            if (recipeItem != null && recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
                SelectionContext selectionContext = buildSelectionContext(recipeInventorySelections);
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