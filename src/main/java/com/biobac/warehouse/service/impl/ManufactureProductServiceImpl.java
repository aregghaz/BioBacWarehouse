package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ManufactureProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.response.ManufactureProductResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ManufactureProductService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.specifications.ManufactureSpecification;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManufactureProductServiceImpl implements ManufactureProductService {
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final ProductHistoryService productHistoryService;
    private final ProductBalanceRepository productBalanceRepository;
    private final ManufactureProductRepository manufactureProductRepository;
    private final ManufactureProductMapper manufactureProductMapper;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final ProductDetailRepository productDetailRepository;

    @Override
    @Transactional
    public ManufactureProductResponse createForProduct(ManufactureProductRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        if (product.getExtraComponents() != null && !product.getExtraComponents().isEmpty()) {
            consumeExtraComponents(product.getExtraComponents(), totalCount);
        }

        if (product.getRecipeItem() != null) {
            consumeRecipeItem(totalCount, product.getRecipeItem());
        }

        ManufactureProduct manufactureProduct = new ManufactureProduct();
        manufactureProduct.setWarehouse(warehouse);
        manufactureProduct.setProduct(product);
        manufactureProduct.setManufacturingDate(request.getManufacturingDate());
        manufactureProduct.setExpirationDate(request.getManufacturingDate().plusDays(product.getExpiration()));
        manufactureProduct.setQuantity(totalCount);

        double totalBefore = product.getInventoryItems() != null
                ? product.getInventoryItems().stream().mapToDouble(i -> i.getQuantity() != null ? i.getQuantity() : 0.0).sum()
                : 0.0;

        ManufactureProduct saved = manufactureProductRepository.save(manufactureProduct);

        increaseBalanceForProduct(warehouse, product, totalCount);
        
        ProductBalance pbForDetail = getOrCreateProductBalance(warehouse, product);
        ProductDetail productDetail = new ProductDetail();
        productDetail.setProductBalance(pbForDetail);
        productDetail.setManufacturingDate(request.getManufacturingDate());
        productDetail.setExpirationDate(request.getManufacturingDate().plusDays(product.getExpiration()));
        productDetail.setQuantity(totalCount);
        productDetailRepository.save(productDetail);

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

        return manufactureProductMapper.toSingleResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ManufactureProductResponse>, PaginationMetadata> getByProductId(Long productId, Map<String, FilterCriteria> filters,
                                                                                     Integer page,
                                                                                     Integer size,
                                                                                     String sortBy,
                                                                                     String sortDir) {
        productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<ManufactureProduct> spec = ManufactureSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("product", JoinType.LEFT).get("id").in(productId));

        Page<ManufactureProduct> pageResult = manufactureProductRepository.findAll(spec, pageable);

        List<ManufactureProductResponse> content = pageResult.getContent()
                .stream()
                .map(manufactureProductMapper::toSingleResponse)
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
                .table("manufactureProductTable")
                .build();

        return Pair.of(content, metadata);
    }

    private IngredientBalance getOrCreateIngredientBalance(Warehouse warehouse, Ingredient ingredient) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (ingredient)");
        }
        if (ingredient == null || ingredient.getId() == null) {
            throw new InvalidDataException("Ingredient is required for component balance");
        }
        return ingredientBalanceRepository.findByWarehouseIdAndIngredientId(warehouse.getId(), ingredient.getId())
                .orElseGet(() -> {
                    IngredientBalance cb = new IngredientBalance();
                    cb.setWarehouse(warehouse);
                    cb.setIngredient(ingredient);
                    cb.setBalance(0.0);
                    return ingredientBalanceRepository.save(cb);
                });
    }

    private ProductBalance getOrCreateProductBalance(Warehouse warehouse, Product product) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (product)");
        }
        if (product == null || product.getId() == null) {
            throw new InvalidDataException("Product is required for component balance");
        }
        return productBalanceRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
                .orElseGet(() -> {
                    ProductBalance cb = new ProductBalance();
                    cb.setWarehouse(warehouse);
                    cb.setProduct(product);
                    cb.setBalance(0.0);
                    return productBalanceRepository.save(cb);
                });
    }

    private void increaseBalanceForProduct(Warehouse warehouse, Product product, double qty) {
        if (qty == 0) return;
        ProductBalance cb = getOrCreateProductBalance(warehouse, product);
        double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
        double after = before + qty;
        cb.setBalance(after);
        productBalanceRepository.save(cb);
    }

    private void consumeIngredientRecursive(Ingredient ingredient, double requiredQty, SelectionContext selectionContext, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason) {
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
            IngredientBalance ingredientBalance = getOrCreateIngredientBalance(defWh, ingredient);
            double before = ingredientBalance.getBalance() != null ? ingredientBalance.getBalance() : 0.0;

            deductFromIngredientDetails(ingredientBalance, requiredQty);
            double after = before - requiredQty;
            ingredientBalance.setBalance(after);
            ingredientBalanceRepository.save(ingredientBalance);

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

    private void consumeProductRecursive(Product product, double requiredQty, SelectionContext selectionContext, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason) {
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
            ProductBalance cb = getOrCreateProductBalance(defWh, product);
            double before = cb.getBalance() != null ? cb.getBalance() : 0.0;
            deductFromProductDetails(cb, requiredQty);
            double after = before - requiredQty;
            cb.setBalance(after);
            productBalanceRepository.save(cb);

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

    private void deductFromIngredientDetails(IngredientBalance ingredientBalance, double requiredQty) {
        if (requiredQty <= 0) return;
        if (ingredientBalance == null || ingredientBalance.getId() == null) {
            throw new InvalidDataException("Ingredient balance is not persisted");
        }
        List<IngredientDetail> batches = ingredientDetailRepository
                .findByIngredientBalanceIdOrderByExpirationDateAsc(ingredientBalance.getId());
        double remaining = requiredQty;
        for (IngredientDetail d : batches) {
            if (remaining <= 0) break;
            double qty = d.getQuantity() != null ? d.getQuantity() : 0.0;
            if (qty <= 0) continue;
            double take = Math.min(qty, remaining);
            d.setQuantity(qty - take);
            ingredientDetailRepository.save(d);
            remaining -= take;
        }
    }

    private void deductFromProductDetails(ProductBalance productBalance, double requiredQty) {
        if (requiredQty <= 0) return;
        if (productBalance == null || productBalance.getId() == null) {
            throw new InvalidDataException("Product balance is not persisted");
        }
        List<ProductDetail> batches = productDetailRepository
                .findByProductBalanceIdOrderByExpirationDateAsc(productBalance.getId());
        double remaining = requiredQty;
        for (ProductDetail d : batches) {
            if (remaining <= 0) break;
            double qty = d.getQuantity() != null ? d.getQuantity() : 0.0;
            if (qty <= 0) continue;
            double take = Math.min(qty, remaining);
            d.setQuantity(qty - take);
            productDetailRepository.save(d);
            remaining -= take;
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
}
