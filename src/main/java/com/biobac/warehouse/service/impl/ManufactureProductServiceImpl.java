package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ManufactureProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.request.ManufactureCalculateRequest;
import com.biobac.warehouse.request.ManufactureProductRequest;
import com.biobac.warehouse.response.ManufactureCalculateResponse;
import com.biobac.warehouse.response.ManufactureProductResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ManufactureProductService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.IngredientBalanceUtil;
import com.biobac.warehouse.utils.SelfWorthPriceUtil;
import com.biobac.warehouse.utils.specifications.ManufactureSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
    private final ManufactureComponentRepository manufactureComponentRepository;
    private final HistoryActionRepository historyActionRepository;
    private final GroupUtil groupUtil;
    private final IngredientRepository ingredientRepository;
    private final IngredientBalanceUtil ingredientBalanceUtil;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int safeSize = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        if (safeSize > 1000) safeSize = 1000;

        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy.trim();
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir.trim();

        String mappedSortBy = mapSortField(safeSortBy);

        Sort sort = safeSortDir.equalsIgnoreCase("asc")
                ? Sort.by(mappedSortBy).ascending()
                : Sort.by(mappedSortBy).descending();

        return PageRequest.of(safePage, safeSize, sort);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "warehouseName" -> "warehouse.name";
            case "productName" -> "product.name";
            case "unitName" -> "product.unit.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional
    public ManufactureProductResponse createForProduct(ManufactureProductRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        double totalCount = request.getQuantity();

        ManufactureProduct manufactureProduct = new ManufactureProduct();
        manufactureProduct.setWarehouse(warehouse);
        manufactureProduct.setProduct(product);
        manufactureProduct.setManufacturingDate(request.getManufacturingDate());
        manufactureProduct.setQuantity(totalCount);
        if (product.getExpiration() != null) {
            manufactureProduct.setExpirationDate(request.getManufacturingDate().plusDays(product.getExpiration()));
        } else {
            manufactureProduct.setExpirationDate(null);
        }

        double totalBefore = getOrCreateProductBalance(warehouse, product).getBalance();

        ManufactureProduct saved = manufactureProductRepository.save(manufactureProduct);

        BigDecimal totalCost = BigDecimal.ZERO;
        if (product.getExtraComponents() != null && !product.getExtraComponents().isEmpty()) {
            totalCost = totalCost.add(consumeExtraComponents(product.getExtraComponents(), totalCount, saved));
        }
        if (product.getRecipeItem() != null) {
            totalCost = totalCost.add(consumeRecipeItem(totalCount, product.getRecipeItem(), saved));
        }

        increaseBalanceForProduct(warehouse, product, totalCount);

        ProductBalance pbForDetail = getOrCreateProductBalance(warehouse, product);
        ProductDetail productDetail = new ProductDetail();
        productDetail.setProductBalance(pbForDetail);
        productDetail.setManufacturingDate(request.getManufacturingDate());
        if (product.getExpiration() != null) {
            productDetail.setExpirationDate(request.getManufacturingDate().plusDays(product.getExpiration()));
        } else {
            productDetail.setExpirationDate(null);
        }
        productDetail.setQuantity(totalCount);
        if (totalCount > 0) {
            BigDecimal unitCost = totalCost.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
            productDetail.setPrice(unitCost);
            saved.setPrice(unitCost);
            manufactureProductRepository.save(saved);
        }
        productDetailRepository.save(productDetail);

        if (totalCount > 0) {
            String warehouseNote = saved.getWarehouse() != null && saved.getWarehouse().getId() != null
                    ? " на складе " + saved.getWarehouse().getName()
                    : "";

            ProductHistoryDto ph = new ProductHistoryDto();
            HistoryAction action = historyActionRepository.findById(3L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));
            ph.setTimestamp(request.getManufacturingDate());
            ph.setProduct(product);
            ph.setAction(action);
            ph.setWarehouse(warehouse);
            ph.setQuantityChange(totalCount);
            ph.setNotes("Произведено" + warehouseNote + " в количестве " + totalCount);

            productHistoryService.recordQuantityChange(ph);
        }


        return manufactureProductMapper.toSingleResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ManufactureProductResponse>, PaginationMetadata> getByProductId(Map<String, FilterCriteria> filters,
                                                                                     Integer page,
                                                                                     Integer size,
                                                                                     String sortBy,
                                                                                     String sortDir) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ManufactureProduct> spec = ManufactureSpecification.buildSpecification(filters)
                .and(ManufactureSpecification.belongsToProductGroups(productGroupIds))
                .and(ManufactureSpecification.belongsToWarehouseGroups(warehouseGroupIds));

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

    @Override
    @Transactional(readOnly = true)
    public List<ManufactureCalculateResponse> calculateProductions(List<ManufactureCalculateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        Map<String, ManufactureCalculateResponse> aggregated = new HashMap<>();

        for (ManufactureCalculateRequest req : requests) {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            double multiplyQty = req.getQuantity() != null ? req.getQuantity() : 1.0;

            RecipeItem recipeItem = product.getRecipeItem();
            if (recipeItem != null && recipeItem.getComponents() != null) {
                for (RecipeComponent rc : recipeItem.getComponents()) {
                    addComponent(aggregated, rc.getIngredient(), rc.getProduct(),
                            rc.getQuantity() * multiplyQty);
                }
            }

            List<ProductComponent> extras = product.getExtraComponents();
            if (extras != null) {
                for (ProductComponent pc : extras) {
                    addComponent(aggregated, pc.getIngredient(), pc.getChildProduct(),
                            pc.getQuantity() * multiplyQty);
                }
            }
        }

        for (ManufactureCalculateResponse resp : aggregated.values()) {

            double balance = fetchBalance(resp);
            resp.setBalanceQuantity(balance);

            BigDecimal unitPrice = fetchUnitPrice(resp);

            BigDecimal requiredTotal = unitPrice
                    .multiply(BigDecimal.valueOf(resp.getRequiredQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal availableTotal = unitPrice
                    .multiply(BigDecimal.valueOf(balance))
                    .setScale(2, RoundingMode.HALF_UP);

            resp.setRequiredPrice(requiredTotal);
            resp.setAvailablePrice(availableTotal);
        }

        return new ArrayList<>(aggregated.values());
    }

    private void addComponent(Map<String, ManufactureCalculateResponse> map,
                              Ingredient ingredient,
                              Product product,
                              double quantity) {

        String key;
        String name = null;

        Long ingrId;
        Long prodId;

        if (ingredient != null) {
            prodId = null;
            key = "INGR-" + ingredient.getId();
            ingrId = ingredient.getId();
            name = ingredient.getName();
        } else {
            ingrId = null;
            key = "PROD-" + product.getId();
            prodId = product.getId();
            name = product.getName();
        }

        String finalName = name;
        ManufactureCalculateResponse resp =
                map.computeIfAbsent(key, k -> {
                    ManufactureCalculateResponse r = new ManufactureCalculateResponse();
                    r.setRequiredQuantity(0.0);
                    r.setIngredientId(ingrId);
                    r.setProductId(prodId);
                    r.setComponentName(finalName);
                    return r;
                });

        resp.setRequiredQuantity(resp.getRequiredQuantity() + quantity);
    }

    private double fetchBalance(ManufactureCalculateResponse resp) {

        if (resp.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(resp.getIngredientId()).orElse(null);
            if (ingredient != null && ingredient.getDefaultWarehouse() != null) {
                return ingredientBalanceRepository
                        .findByWarehouseAndIngredient(ingredient.getDefaultWarehouse(), ingredient)
                        .map(IngredientBalance::getBalance)
                        .orElse(0.0);
            }
        } else if (resp.getProductId() != null) {
            Product product = productRepository.findById(resp.getProductId()).orElse(null);
            if (product != null && product.getDefaultWarehouse() != null) {
                return productBalanceRepository
                        .findByWarehouseAndProduct(product.getDefaultWarehouse(), product)
                        .map(ProductBalance::getBalance)
                        .orElse(0.0);
            }
        }
        return 0.0;
    }

    private BigDecimal fetchUnitPrice(ManufactureCalculateResponse resp) {

        if (resp.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(resp.getIngredientId()).orElse(null);
            if (ingredient != null && ingredient.getDefaultWarehouse() != null) {
                IngredientBalance bal = ingredientBalanceRepository
                        .findByWarehouseAndIngredient(ingredient.getDefaultWarehouse(), ingredient)
                        .orElse(null);
                return bal != null
                        ? SelfWorthPriceUtil.calculateIngredientPrice(bal)
                        : ingredient.getPrice();
            }
        }

        if (resp.getProductId() != null) {
            Product product = productRepository.findById(resp.getProductId()).orElse(null);
            if (product != null && product.getDefaultWarehouse() != null) {
                ProductBalance bal = productBalanceRepository
                        .findByWarehouseAndProduct(product.getDefaultWarehouse(), product)
                        .orElse(null);
                return bal != null
                        ? SelfWorthPriceUtil.calculateProductPrice(bal)
                        : BigDecimal.ZERO;
            }
        }

        return BigDecimal.ZERO;
    }

    private ProductBalance getOrCreateProductBalance(Warehouse warehouse, Product product) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse is required for component balance (product)");
        }
        if (product == null || product.getId() == null) {
            throw new InvalidDataException("Product is required for component balance");
        }
        return productBalanceRepository.findByWarehouseAndProduct(warehouse, product)
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

    private BigDecimal consumeIngredientRecursive(Ingredient ingredient, double requiredQty, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason, ManufactureProduct manufactureProduct) {
        if (requiredQty <= 0) return BigDecimal.ZERO;
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

            IngredientBalance ingredientBalance = ingredientBalanceUtil.getOrCreateIngredientBalance(defWh, ingredient);
            double before = ingredientBalance.getBalance() != null ? ingredientBalance.getBalance() : 0.0;

            BigDecimal cost = deductFromIngredientDetails(ingredientBalance, requiredQty, manufactureProduct);
            double after = before - requiredQty;
            ingredientBalance.setBalance(after);
            ingredientBalanceRepository.save(ingredientBalance);

            String warehouseName = defWh.getName() != null ? defWh.getName() : ("#" + defWh.getId());
            String productInfo = "";
            if (manufactureProduct != null && manufactureProduct.getProduct() != null) {
                String prodName = manufactureProduct.getProduct().getName();
                Long mId = manufactureProduct.getId();
                productInfo = String.format(" для производства продукта \"%s\"", prodName != null ? prodName : "без названия");
                if (mId != null) {
                    productInfo += String.format(" (производство #%d)", mId);
                }
            }
            String note = String.format(
                    "Использовано %.2f единиц ингредиента со склада \"%s\"%s в количестве %.2f",
                    requiredQty, warehouseName, productInfo, requiredQty
            );

            HistoryAction action = historyActionRepository.findById(4L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));
            IngredientHistoryDto ih = new IngredientHistoryDto();
            ih.setAction(action);
            ih.setIngredient(ingredient);
            ih.setWarehouse(defWh);
            ih.setTimestamp(manufactureProduct != null ? manufactureProduct.getManufacturingDate() : null);
            ih.setQuantityChange(after - before);
            ih.setNotes(note);

            ingredientHistoryService.recordQuantityChange(ih);
            return cost;
        } finally {
            if (ingredientId != null) visitingIngredientIds.remove(ingredientId);
        }
    }

    private BigDecimal consumeProductRecursive(Product product, double requiredQty, Set<Long> visitingIngredientIds, Set<Long> visitingProductIds, String reason, ManufactureProduct manufactureProduct) {
        if (requiredQty <= 0) return BigDecimal.ZERO;
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

            BigDecimal cost = deductFromProductDetails(cb, requiredQty, manufactureProduct);
            double after = before - requiredQty;
            cb.setBalance(after);
            productBalanceRepository.save(cb);

            String warehouseName = defWh.getName() != null ? defWh.getName() : ("#" + defWh.getId());
            String productInfo = "";

            if (manufactureProduct != null && manufactureProduct.getProduct() != null) {
                String parentName = manufactureProduct.getProduct().getName();
                Long mId = manufactureProduct.getId();
                productInfo = String.format(" для производства продукта \"%s\"",
                        parentName != null ? parentName : "без названия");
                if (mId != null) {
                    productInfo += String.format(" (производство #%d)", mId);
                }
            }

            String note = String.format(
                    "Израсходовано %.2f единиц продукта со склада \"%s\"%s в количестве %.2f",
                    requiredQty, warehouseName, productInfo, requiredQty
            );

            HistoryAction action = historyActionRepository.findById(4L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));

            ProductHistoryDto phDec = new ProductHistoryDto();
            phDec.setAction(action);
            phDec.setProduct(product);
            phDec.setWarehouse(defWh);
            phDec.setQuantityChange(after - before);
            phDec.setNotes(note);

            productHistoryService.recordQuantityChange(phDec);
            return cost;
        } finally {
            if (productId != null) visitingProductIds.remove(productId);
        }
    }

    private BigDecimal consumeExtraComponents(List<ProductComponent> components, double totalCount, ManufactureProduct manufactureProduct) {
        if (components == null || components.isEmpty()) return BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

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
                totalCost = totalCost.add(consumeIngredientRecursive(compIng, required, new HashSet<>(), new HashSet<>(), "Consumed for extra components", manufactureProduct));
            } else {
                totalCost = totalCost.add(consumeProductRecursive(compProd, required, new HashSet<>(), new HashSet<>(), "Consumed for extra components", manufactureProduct));
            }
        }
        return totalCost;
    }

    private BigDecimal consumeRecipeItem(double totalCount, RecipeItem recipeItem, ManufactureProduct manufactureProduct) {
        BigDecimal totalCost = BigDecimal.ZERO;
        if (totalCount > 0) {
            if (recipeItem != null && recipeItem.getComponents() != null && !recipeItem.getComponents().isEmpty()) {
                for (RecipeComponent component : recipeItem.getComponents()) {
                    double perUnit = component.getQuantity() != null ? component.getQuantity() : 0.0;
                    double required = perUnit * totalCount;
                    if (required <= 0) continue;

                    Ingredient compIng = component.getIngredient();
                    Product compProd = component.getProduct();
                    if (compIng != null && compProd == null) {
                        totalCost = totalCost.add(consumeIngredientRecursive(compIng, required, new HashSet<>(), new HashSet<>(), "Consumed for recipe requirements", manufactureProduct));
                    } else if (compProd != null && compIng == null) {
                        totalCost = totalCost.add(consumeProductRecursive(compProd, required, new HashSet<>(), new HashSet<>(), "Consumed for recipe requirements", manufactureProduct));
                    } else {
                        throw new InvalidDataException("Recipe component must be either ingredient or product");
                    }
                }
            }
        }
        return totalCost;
    }

    private BigDecimal deductFromIngredientDetails(IngredientBalance ingredientBalance, double requiredQty, ManufactureProduct manufactureProduct) {
        if (requiredQty <= 0) return BigDecimal.ZERO;
        if (ingredientBalance == null || ingredientBalance.getId() == null) {
            throw new InvalidDataException("Ingredient balance is not persisted");
        }
        List<IngredientDetail> batches = ingredientDetailRepository
                .findByIngredientBalanceIdOrderByExpirationDateAsc(ingredientBalance.getId());
        double remaining = requiredQty;
        BigDecimal cost = BigDecimal.ZERO;
        for (IngredientDetail d : batches) {
            if (remaining <= 0) break;
            double qty = d.getQuantity() != null ? d.getQuantity() : 0.0;
            if (qty <= 0) continue;
            double take = Math.min(qty, remaining);
            BigDecimal unitPrice = d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO;
            double newQty = qty - take;
            if (newQty <= 0) {
                ingredientDetailRepository.delete(d);
            } else {
                d.setQuantity(newQty);
                ingredientDetailRepository.save(d);
            }
            remaining -= take;

            cost = cost.add(unitPrice.multiply(BigDecimal.valueOf(take)));

            ManufactureComponent mc = new ManufactureComponent();
            mc.setManufactureProduct(manufactureProduct);
            mc.setIngredient(ingredientBalance.getIngredient());
            mc.setQuantity(take);
            mc.setUnitPrice(unitPrice);
            manufactureComponentRepository.save(mc);
        }
        return cost;
    }

    private BigDecimal deductFromProductDetails(ProductBalance productBalance, double requiredQty, ManufactureProduct manufactureProduct) {
        if (requiredQty <= 0) return BigDecimal.ZERO;
        if (productBalance == null || productBalance.getId() == null) {
            throw new InvalidDataException("Product balance is not persisted");
        }
        List<ProductDetail> batches = productDetailRepository
                .findByProductBalanceIdOrderByExpirationDateAsc(productBalance.getId());
        double remaining = requiredQty;
        BigDecimal cost = BigDecimal.ZERO;
        for (ProductDetail d : batches) {
            if (remaining <= 0) break;
            double qty = d.getQuantity() != null ? d.getQuantity() : 0.0;
            if (qty <= 0) continue;
            double take = Math.min(qty, remaining);
            BigDecimal unitPrice = d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO;
            double newQty = qty - take;
            if (newQty <= 0) {
                productDetailRepository.delete(d);
            } else {
                d.setQuantity(newQty);
                productDetailRepository.save(d);
            }
            remaining -= take;

            cost = cost.add(unitPrice.multiply(BigDecimal.valueOf(take)));

            ManufactureComponent mc = new ManufactureComponent();
            mc.setManufactureProduct(manufactureProduct);
            mc.setProduct(productBalance.getProduct());
            mc.setQuantity(take);
            mc.setUnitPrice(unitPrice);
            manufactureComponentRepository.save(mc);
        }
        return cost;
    }
}