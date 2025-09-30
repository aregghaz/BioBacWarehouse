package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.client.AttributeClient;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.DuplicateException;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.ProductResponse;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductComponentRepository productComponentRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final RecipeItemRepository recipeItemRepository;
    private final RecipeComponentRepository recipeComponentRepository;
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
        Product existingProduct = productRepository.findBySku(request.getSku());
        if (existingProduct != null) {
            throw new DuplicateException("provided Sku already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());

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
                product.getUnitTypeConfigs().add(baseLink);
            }
        }

        if (request.getExtraComponents() != null && !request.getExtraComponents().isEmpty()) {
            for (ProductAdditionalComponents compReq : request.getExtraComponents()) {
                ProductComponent component = new ProductComponent();
                component.setProduct(product);

                if (compReq.getIngredientId() != null) {
                    Ingredient ingredient = ingredientRepository.findById(compReq.getIngredientId())
                            .orElseThrow(() -> new NotFoundException("Ingredient not found"));
                    component.setIngredient(ingredient);
                }

                if (compReq.getProductId() != null) {
                    Product childProduct = productRepository.findById(compReq.getProductId())
                            .orElseThrow(() -> new NotFoundException("Product not found"));
                    component.setChildProduct(childProduct);
                }

                productComponentRepository.save(component);
            }
        }

        Product saved = productRepository.save(product);

        productHistoryService.recordQuantityChange(saved, 0.0, 0.0, "CREATED", "Added new product to system");

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

        if (request.getRecipeItemId() != null) {
            RecipeItem recipeItem = recipeItemRepository.findById(request.getRecipeItemId())
                    .orElseThrow(() -> new NotFoundException("Recipe not found"));
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

                    if (compReq.getIngredientId() != null) {
                        Ingredient ingredient = ingredientRepository.findById(compReq.getIngredientId())
                                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
                        component.setIngredient(ingredient);
                    }

                    if (compReq.getProductId() != null) {
                        Product childProduct = productRepository.findById(compReq.getProductId())
                                .orElseThrow(() -> new NotFoundException("Product not found"));
                        if (Objects.equals(existing.getId(), childProduct.getId())) {
                            throw new DuplicateException("Parent product can't be part of extra component");
                        }
                        component.setChildProduct(childProduct);
                    }

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
        return productRepository.findAllByDeletedFalse().stream().map(productMapper::toResponse).collect(Collectors.toList());
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
            product.setRecipeItem(null);
            if (recipeItem.getProducts() != null) {
                recipeItem.getProducts().remove(product);
            }
            recipeItemRepository.save(recipeItem);
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

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllExcludeRecipeIngredient(Long recipeItemId) {
        List<Product> products = productRepository.findAllByDeletedFalseExcludeRecipe(recipeItemId);

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }
}