package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotEnoughException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ProductCreateRequest;
import com.biobac.warehouse.request.ProductUpdateRequest;
import com.biobac.warehouse.request.UnitTypeConfigRequest;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.utils.specifications.ProductSpecification;
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
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final ProductGroupRepository productGroupRepository;
    private final ProductMapper productMapper;
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
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double requiredQty = perUnit * multiplier;
                    if (requiredQty > 0) {
                        Ingredient compIng = component.getIngredient();
                        Product compProd = component.getProduct();
                        if (compIng != null && compProd == null) {
                            consumeIngredientRecursive(compIng, requiredQty, new HashSet<>(), new HashSet<>());
                        } else if (compProd != null && compIng == null) {
                            consumeProductRecursive(compProd, requiredQty, new HashSet<>(), new HashSet<>());
                        } else {
                            throw new InvalidDataException("Recipe component must be either ingredient or product");
                        }
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
        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            product.setUnit(unit);
        }
        if (request.getProductGroupId() != null) {
            ProductGroup productGroup = productGroupRepository.findById(request.getProductGroupId())
                    .orElseThrow(() -> new NotFoundException("ProductGroup not found"));
            product.setProductGroup(productGroup);
        }

        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = product.getUnit() != null && product.getUnit().getUnitTypes() != null
                    ? product.getUnit().getUnitTypes() : new HashSet<>();
            product.getUnitTypeConfigs().clear();
            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));
                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                ProductUnitType link = new ProductUnitType();
                link.setProduct(product);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                product.getUnitTypeConfigs().add(link);
            }
        }

        product.getInventoryItems().add(inventoryItem);
        Product saved = productRepository.save(product);
        inventoryItemRepository.save(inventoryItem);

        double addedQty = request.getQuantity() != null ? request.getQuantity() : 0.0;
        if (addedQty > 0) {
            productHistoryService.recordQuantityChange(saved, 0.0, addedQty, "INCREASE", "Initial stock added during product creation");
        }

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeClient.createValues(saved.getId(), AttributeTargetType.PRODUCT.name(), request.getAttributes());
        }
        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            saved.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAllByDeletedFalse().stream().map(productMapper::toResponse).collect(Collectors.toList());
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

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
            inventoryNeedsUpdate = true;
        }

        if (request.getProductGroupId() != null) {
            ProductGroup productGroup = productGroupRepository.findById(request.getProductGroupId())
                    .orElseThrow(() -> new NotFoundException("ProductGroup not found"));
            existing.setProductGroup(productGroup);
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            recipeItem.setProduct(existing);
            existing.setRecipeItem(recipeItem);
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
                ProductUnitType link = new ProductUnitType();
                link.setProduct(existing);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                existing.getUnitTypeConfigs().add(link);
            }
        }
        if (request.getAttributeGroupIds() != null) {
            existing.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        Product saved = productRepository.save(existing);
        if (inventoryNeedsUpdate && items != null && !items.isEmpty()) {
            inventoryItemRepository.saveAll(items);
        }

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeClient.createValues(saved.getId(), AttributeTargetType.PRODUCT.name(), request.getAttributes());
        }

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Product> spec = ProductSpecification.buildSpecification(filters);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> content = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        attributeClient.deleteValues(id, AttributeTargetType.PRODUCT.name());

        double totalBefore = 0.0;
        List<InventoryItem> beforeItems = product.getInventoryItems();
        if (beforeItems != null) {
            totalBefore = beforeItems.stream()
                    .mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0)
                    .sum();
        }

        RecipeItem recipeItem = product.getRecipeItem();
        if (recipeItem != null) {
            recipeItem.setProduct(null);
            recipeItemRepository.save(recipeItem);
            product.setRecipeItem(null);
        }

        List<RecipeComponent> productComponents = recipeComponentRepository.findByProductId(id);
        if (productComponents != null && !productComponents.isEmpty()) {
            for (RecipeComponent rc : productComponents) {
                rc.setProduct(null);
            }
            recipeComponentRepository.saveAll(productComponents);
        }

        List<InventoryItem> items = product.getInventoryItems();
        if (items != null && !items.isEmpty()) {
            inventoryItemRepository.deleteAll(items);
            product.getInventoryItems().clear();
        }

        product.setDeleted(true);
        productRepository.save(product);

        productHistoryService.recordQuantityChange(product, totalBefore, 0.0, "DELETE", "Soft deleted");
    }


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

            RecipeItem subRecipe = ingredient.getRecipeItem();
            if (subRecipe == null || subRecipe.getComponents() == null || subRecipe.getComponents().isEmpty()) {
                throw new NotEnoughException("Not enough ingredient '" + ingredient.getName() + "' to cover required quantity: " + requiredQty);
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
                productHistoryService.recordQuantityChange(product, available, available - toConsume, "DECREASE", "Consumed for recipe requirements");
                remaining -= toConsume;
            }

            if (remaining <= 0) return;

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