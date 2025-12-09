package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.DuplicateException;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.ProductResponse;
import com.biobac.warehouse.response.UnitTypeCalculatedResponse;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.service.ProductService;
import com.biobac.warehouse.service.UnitTypeCalculator;
import com.biobac.warehouse.utils.GroupUtil;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService, UnitTypeCalculator {
    private final ProductComponentRepository productComponentRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final RecipeComponentRepository recipeComponentRepository;
    private final ProductHistoryService productHistoryService;
    private final UnitRepository unitRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final ProductGroupRepository productGroupRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductBalanceRepository productBalanceRepository;
    private final HistoryActionRepository historyActionRepository;
    private final ProductMapper productMapper;
    private final AttributeClient attributeClient;
    private final GroupUtil groupUtil;

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
            case "productGroupName" -> "productGroup.name";
            case "unitName" -> "unit.name";
            case "recipeItemName" -> "recipeItem.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        boolean existingProduct = productRepository.existsBySkuOrName(request.getSku(), request.getName());
        if (existingProduct) {
            throw new DuplicateException("provided Sku already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setMinimalBalance(request.getMinimalBalance() != null ? request.getMinimalBalance() : 0);

        if (request.getExpiration() != null) {
            product.setExpiration(request.getExpiration());
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            product.setRecipeItem(recipeItem);
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            product.setUnit(unit);
        }

        if (request.getProductGroupId() != null) {
            ProductGroup productGroup = productGroupRepository.findById(request.getProductGroupId())
                    .orElseThrow(() -> new NotFoundException("Product Group not found"));
            product.setProductGroup(productGroup);
        }

        if (request.getDefaultWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getDefaultWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            product.setDefaultWarehouse(warehouse);
        }

        if (product.getUnit() != null) {
            Unit unit = product.getUnit();

            UnitType baseUnitType = unitTypeRepository.findByName(unit.getName())
                    .orElseGet(() -> {
                        UnitType newType = new UnitType();
                        newType.setName(unit.getName());
                        return unitTypeRepository.save(newType);
                    });

            boolean alreadyExists = product.getUnitTypeConfigs().stream()
                    .anyMatch(link -> link.getUnitType().equals(baseUnitType));

            if (!alreadyExists) {
                ProductUnitType baseLink = new ProductUnitType();
                baseLink.setProduct(product);
                baseLink.setUnitType(baseUnitType);
                baseLink.setSize(1.0);
                baseLink.setBaseType(true);
                product.getUnitTypeConfigs().add(baseLink);
            }
        }

        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = product.getUnit() != null && product.getUnit().getUnitTypes() != null
                    ? product.getUnit().getUnitTypes() : new HashSet<>();
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

        if (request.getExtraComponents() != null && !request.getExtraComponents().isEmpty()) {
            for (ProductAdditionalComponents compReq : request.getExtraComponents()) {
                ProductComponent component = new ProductComponent();
                component.setProduct(product);

                boolean hasIng = compReq.getIngredientId() != null;
                boolean hasProd = compReq.getProductId() != null;
                if (hasIng == hasProd) {
                    throw new InvalidDataException("Extra component must reference exactly one of ingredientId or productId");
                }

                if (hasIng) {
                    Ingredient ingredient = ingredientRepository.findById(compReq.getIngredientId())
                            .orElseThrow(() -> new NotFoundException("Ingredient not found"));
                    component.setIngredient(ingredient);
                } else {
                    Product childProduct = productRepository.findById(compReq.getProductId())
                            .orElseThrow(() -> new NotFoundException("Product not found"));
                    component.setChildProduct(childProduct);
                }

                component.setQuantity(compReq.getQuantity());

                productComponentRepository.save(component);
            }
        }

        Product saved = productRepository.save(product);

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributeClient.createValues(saved.getId(), AttributeTargetType.PRODUCT.name(), request.getAttributes());
        }

        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            saved.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (request.getName() != null) existing.setName(request.getName());

        if (request.getDescription() != null) existing.setDescription(request.getDescription());

        if (request.getSku() != null) existing.setSku(request.getSku());

        if (request.getExpiration() != null) {
            existing.setExpiration(request.getExpiration());
        }

        if (request.getMinimalBalance() != null) {
            existing.setMinimalBalance(request.getMinimalBalance());
        }

        if (request.getUnitId() != null) {
            Unit unit = unitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new NotFoundException("Unit not found"));
            existing.setUnit(unit);
        }

        if (request.getProductGroupId() != null) {
            ProductGroup productGroup = productGroupRepository.findById(request.getProductGroupId())
                    .orElseThrow(() -> new NotFoundException("ProductGroup not found"));
            existing.setProductGroup(productGroup);
        }

        if (request.getDefaultWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getDefaultWarehouseId())
                    .orElseThrow(() -> new NotFoundException("Warehouse not found"));
            existing.setDefaultWarehouse(warehouse);
        }

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
            existing.setRecipeItem(recipeItem);
        }

        if (request.getUnitTypeConfigs() != null) {
            Set<UnitType> allowedTypes = existing.getUnit() != null && existing.getUnit().getUnitTypes() != null
                    ? existing.getUnit().getUnitTypes() : new HashSet<>();

            existing.getUnitTypeConfigs().clear();

            UnitType baseUnitType = null;
            if (existing.getUnit() != null) {
                Unit unit = existing.getUnit();
                baseUnitType = unitTypeRepository.findByName(unit.getName())
                        .orElseGet(() -> {
                            UnitType newType = new UnitType();
                            newType.setName(unit.getName());
                            return unitTypeRepository.save(newType);
                        });
                ProductUnitType baseLink = new ProductUnitType();
                baseLink.setProduct(existing);
                baseLink.setUnitType(baseUnitType);
                baseLink.setSize(1.0);
                baseLink.setBaseType(true);
                existing.getUnitTypeConfigs().add(baseLink);
            }

            for (UnitTypeConfigRequest cfgReq : request.getUnitTypeConfigs()) {
                if (cfgReq.getUnitTypeId() == null) {
                    throw new InvalidDataException("unitTypeId is required in unitTypeConfigs");
                }
                UnitType ut = unitTypeRepository.findById(cfgReq.getUnitTypeId())
                        .orElseThrow(() -> new NotFoundException("UnitType not found"));

                if (ut.equals(baseUnitType)) {
                    continue;
                }

                if (!allowedTypes.isEmpty() && !allowedTypes.contains(ut)) {
                    throw new InvalidDataException("UnitType '" + ut.getName() + "' is not allowed for selected Unit");
                }
                ProductUnitType link = new ProductUnitType();
                link.setProduct(existing);
                link.setUnitType(ut);
                link.setSize(cfgReq.getSize());
                link.setBaseType(false);
                existing.getUnitTypeConfigs().add(link);
            }
        }
        if (request.getAttributeGroupIds() != null) {
            existing.setAttributeGroupIds(request.getAttributeGroupIds());
        }

        if (request.getExtraComponents() != null) {
            List<ProductComponent> current = existing.getExtraComponents();
            if (current != null && !current.isEmpty()) {
                productComponentRepository.deleteAll(current);
                current.clear();
            }
            if (!request.getExtraComponents().isEmpty()) {
                for (ProductAdditionalComponents compReq : request.getExtraComponents()) {
                    ProductComponent component = new ProductComponent();
                    component.setProduct(existing);

                    boolean hasIng = compReq.getIngredientId() != null;
                    boolean hasProd = compReq.getProductId() != null;
                    if (hasIng == hasProd) {
                        throw new InvalidDataException("Extra component must reference exactly one of ingredientId or productId");
                    }

                    if (hasIng) {
                        Ingredient ingredient = ingredientRepository.findById(compReq.getIngredientId())
                                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
                        component.setIngredient(ingredient);
                    } else {
                        Product childProduct = productRepository.findById(compReq.getProductId())
                                .orElseThrow(() -> new NotFoundException("Product not found"));
                        if (Objects.equals(existing.getId(), childProduct.getId())) {
                            throw new DuplicateException("Parent product can't be part of extra component");
                        }
                        component.setChildProduct(childProduct);
                    }

                    component.setQuantity(compReq.getQuantity());

                    productComponentRepository.save(component);
                }
            }
        }

        Product saved = productRepository.save(existing);

        List<AttributeUpsertRequest> attributes = request.getAttributeGroupIds() == null || request.getAttributeGroupIds().isEmpty() ? Collections.emptyList() : request.getAttributes();

        attributeClient.updateValues(saved.getId(), AttributeTargetType.PRODUCT.name(), request.getAttributeGroupIds(), attributes);

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
        List<Long> groupIds = groupUtil.getAccessibleProductGroupIds();

        Specification<Product> spec = ProductSpecification.belongsToGroups(groupIds)
                .and(ProductSpecification.isDeleted());
        return productRepository.findAll(spec).stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                         Integer page,
                                                                         Integer size,
                                                                         String sortBy,
                                                                         String sortDir) {
        List<Long> groupIds = groupUtil.getAccessibleProductGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Product> spec = ProductSpecification.buildSpecification(filters)
                .and(ProductSpecification.belongsToGroups(groupIds));
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

        product.setDeleted(true);
        productRepository.save(product);

        ProductHistoryDto phDelete = new ProductHistoryDto();
        phDelete.setProduct(product);
        phDelete.setWarehouse(product.getDefaultWarehouse());
        phDelete.setQuantityChange(0.0);
        phDelete.setNotes("Soft deleted");
        productHistoryService.recordQuantityChange(phDelete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllExcludeRecipeIngredient(Long recipeItemId) {
        List<Product> products = productRepository.findAllByDeletedFalseExcludeRecipe(recipeItemId);

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitTypeCalculatedResponse> calculateUnitTypes(Long id, InventoryUnitTypeRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        ProductUnitType config = product.getUnitTypeConfigs().stream()
                .filter(c -> c.getId().equals(request.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Unit Type Config not found for this product"));

        Double total = config.isBaseType() ? request.getCount() : config.getSize() * request.getCount();

        return product.getUnitTypeConfigs().stream().map(utc -> {
            UnitTypeCalculatedResponse calculatedResponse = new UnitTypeCalculatedResponse();
            calculatedResponse.setUnitTypeName(utc.getUnitType().getName());
            calculatedResponse.setUnitTypeId(utc.getId());
            calculatedResponse.setBaseUnit(utc.isBaseType());
            if (utc.isBaseType()) {
                calculatedResponse.setSize(total);
            } else {
                calculatedResponse.setSize(Math.ceil(total / utc.getSize()));
            }
            return calculatedResponse;
        }).toList();
    }

    @Override
    @Transactional
    public void consumeProductsForSale(List<ProductConsumeSaleRequest> request) {
        if (request == null || request.isEmpty()) {
            return;
        }

        for (ProductConsumeSaleRequest r : request) {
            if (r == null) continue;
            double qty = Optional.ofNullable(r.getQuantity()).orElse(0.0);
            if (qty <= 0) continue;

            Product product = productRepository.findById(r.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));

            Warehouse defWh = product.getDefaultWarehouse();
            if (defWh == null || defWh.getId() == null) {
                throw new InvalidDataException("Default warehouse is not set for product " + product.getName());
            }

            ProductBalance balance = productBalanceRepository.findByWarehouseAndProduct(defWh, product)
                    .orElseThrow(() -> new NotFoundException("Product not found on that warehouse"));

            double before = Optional.ofNullable(balance.getBalance()).orElse(0.0);
            double after = before - qty;
            balance.setBalance(after);
            productBalanceRepository.save(balance);

            HistoryAction action = historyActionRepository.findById(4L)
                    .orElseThrow(() -> new NotFoundException("Action not found"));

            String productName = product.getName() != null ? product.getName() : ("#" + product.getId());
            String whName = defWh.getName() != null ? defWh.getName() : ("#" + defWh.getId());
            String note = String.format(
                    "Продажа: списано %.2f единиц продукта \"%s\" со склада \"%s\" (итог: %.2f)",
                    qty, productName, whName, after
            );

            ProductHistoryDto dto = new ProductHistoryDto();
            dto.setAction(action);
            dto.setProduct(product);
            dto.setWarehouse(defWh);
            dto.setQuantityChange(after - before);
            dto.setNotes(note);
            productHistoryService.recordQuantityChange(dto);
        }
    }
}