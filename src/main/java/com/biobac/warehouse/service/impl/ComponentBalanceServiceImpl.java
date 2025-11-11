package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.mapper.ComponentBalanceMapper;
import com.biobac.warehouse.mapper.IngredientMapper;
import com.biobac.warehouse.mapper.ProductMapper;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.*;
import com.biobac.warehouse.service.ComponentBalanceService;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.specifications.*;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComponentBalanceServiceImpl implements ComponentBalanceService {
    private final ComponentBalanceMapper componentBalanceMapper;
    private final IngredientBalanceRepository ingredientBalanceRepository;
    private final ProductBalanceRepository productBalanceRepository;
    private final ProductDetailRepository productDetailRepository;
    private final IngredientDetailRepository ingredientDetailRepository;
    private final GroupUtil groupUtil;
    private final IngredientRepository ingredientRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final IngredientHistoryService ingredientHistoryService;
    private final ProductHistoryService productHistoryService;
    private final IngredientMapper ingredientMapper;
    private final ProductMapper productMapper;

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
            case "ingredientName" -> "ingredient.name";
            case "ingredientGroupName" -> "ingredient.ingredientGroup.name";
            case "ingredientUnitName" -> "ingredient.unit.name";
            case "ingredientMinimalBalance" -> "ingredient.minimalBalance";
            case "ingredientExpirationDate", "productExpirationDate" -> "details.expirationDate";
            case "warehouseName" -> "warehouse.name";
            case "productName" -> "product.name";
            case "productGroupName" -> "product.productGroup.name";
            case "productUnitName" -> "product.unit.name";
            case "productMinimalBalance" -> "product.minimalBalance";
            case "ingredientDetailName" -> "ingredientBalance.ingredient.name";
            case "ingredientDetailUnitName" -> "ingredientBalance.ingredient.unit.name";
            case "productDetailName" -> "productBalance.product.name";
            case "productDetailUnitName" -> "productBalance.product.unit.name";
            default -> sortBy;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ComponentBalanceIngResponse>, PaginationMetadata> getIngPagination(Map<String, FilterCriteria> filters,
                                                                                        Integer page,
                                                                                        Integer size,
                                                                                        String sortBy,
                                                                                        String sortDir) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<IngredientBalance> spec = IngredientBalanceSpecification.buildSpecification(filters)
                .and(IngredientBalanceSpecification.belongsToIngredientGroups(ingredientGroupIds))
                .and(IngredientBalanceSpecification.belongsToWarehouseGroups(warehouseGroupIds))
                .and((root, query, cb) -> cb.isNotNull(root.get("ingredient")));

        Page<IngredientBalance> componentBalancePage = ingredientBalanceRepository.findAll(spec, pageable);

        List<ComponentBalanceIngResponse> content = componentBalancePage.getContent()
                .stream()
                .map(componentBalanceMapper::toIngResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                componentBalancePage.getNumber(),
                componentBalancePage.getSize(),
                componentBalancePage.getTotalElements(),
                componentBalancePage.getTotalPages(),
                componentBalancePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "ingredientBalanceTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ComponentBalanceProdResponse>, PaginationMetadata> getProdPagination(Map<String, FilterCriteria> filters,
                                                                                          Integer page,
                                                                                          Integer size,
                                                                                          String sortBy,
                                                                                          String sortDir) {
        List<Long> warehouseGroupIds = groupUtil.getAccessibleWarehouseGroupIds();
        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ProductBalance> spec = ProductBalanceSpecification.buildSpecification(filters)
                .and(ProductBalanceSpecification.belongsToWarehouseGroups(warehouseGroupIds))
                .and(ProductBalanceSpecification.belongsToProductGroups(productGroupIds))
                .and((root, query, cb) -> cb.isNotNull(root.get("product")));
        Page<ProductBalance> componentBalancePage = productBalanceRepository.findAll(spec, pageable);

        List<ComponentBalanceProdResponse> content = componentBalancePage.getContent()
                .stream()
                .map(componentBalanceMapper::toProdResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                componentBalancePage.getNumber(),
                componentBalancePage.getSize(),
                componentBalancePage.getTotalElements(),
                componentBalancePage.getTotalPages(),
                componentBalancePage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productBalanceTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    public Pair<List<ProductDetailResponse>, PaginationMetadata> getProductDetailsByProductId(Long id, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<ProductDetail> spec = ProductDetailSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("productBalance", JoinType.LEFT).get("id").in(id));
        Page<ProductDetail> productDetailPage = productDetailRepository.findAll(spec, pageable);

        List<ProductDetailResponse> content = productDetailPage.getContent()
                .stream()
                .map(c -> {
                    ProductDetailResponse response = new ProductDetailResponse();
                    response.setProductDetailName(c.getProductBalance().getProduct().getName());
                    response.setExpirationDate(c.getExpirationDate());
                    response.setQuantity(c.getQuantity());
                    response.setProductDetailUnitName(c.getProductBalance().getProduct().getUnit().getName());
                    response.setManufacturingDate(c.getManufacturingDate());
                    response.setPrice(c.getPrice());
                    return response;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                productDetailPage.getNumber(),
                productDetailPage.getSize(),
                productDetailPage.getTotalElements(),
                productDetailPage.getTotalPages(),
                productDetailPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productBalanceTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    public Pair<List<IngredientDetailResponse>, PaginationMetadata> getIngredientDetailsByIngredientId(Long id, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<IngredientDetail> spec = IngredientDetailSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredientBalance", JoinType.LEFT).get("id").in(id));
        Page<IngredientDetail> ingredientDetailPage = ingredientDetailRepository.findAll(spec, pageable);

        List<IngredientDetailResponse> content = ingredientDetailPage.getContent()
                .stream()
                .map(c -> {
                    IngredientDetailResponse response = new IngredientDetailResponse();
                    response.setIngredientDetailName(c.getIngredientBalance().getIngredient().getName());
                    response.setExpirationDate(c.getExpirationDate());
                    response.setIngredientDetailUnitName(c.getIngredientBalance().getIngredient().getUnit().getName());
                    response.setQuantity(c.getQuantity());
                    response.setManufacturingDate(c.getManufacturingDate());
                    response.setPrice(c.getPrice());
                    response.setImportDate(c.getImportDate());
                    return response;
                })
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientDetailPage.getNumber(),
                ingredientDetailPage.getSize(),
                ingredientDetailPage.getTotalElements(),
                ingredientDetailPage.getTotalPages(),
                ingredientDetailPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "productBalanceTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getIngredientBalance(Long ingredientId, Long warehouseId, LocalDateTime date) {
        Double balance = ingredientHistoryService.getEventualForIngredient(ingredientId, warehouseId, date);
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ComponentBalanceQuantityResponse getProductBalance(Long productId, Long warehouseId, LocalDateTime date) {
        Double balance = productHistoryService.getEventualForProduct(productId, warehouseId, date);
        ComponentBalanceQuantityResponse response = new ComponentBalanceQuantityResponse();
        response.setBalance(balance);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponse> getRelatedIngredients(Long warehouseId) {
        List<Long> ids = ingredientBalanceRepository.findAll()
                .stream()
                .filter(i -> i.getWarehouse().getId().equals(warehouseId))
                .map(f -> f.getIngredient().getId()).toList();

        List<Long> ingredientGroupIds = groupUtil.getAccessibleIngredientGroupIds();
        Specification<Ingredient> spec = IngredientSpecification.belongsToGroups(ingredientGroupIds)
                .and(IngredientSpecification.isDeleted())
                .and(IngredientSpecification.containIds(ids));

        return ingredientRepository.findAll(spec)
                .stream().map(ingredientMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long warehouseId) {
        List<Long> ids = productBalanceRepository.findAll()
                .stream()
                .filter(i -> i.getWarehouse().getId().equals(warehouseId))
                .map(f -> f.getProduct().getId()).toList();

        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Specification<Product> spec = ProductSpecification.belongsToGroups(productGroupIds)
                .and(ProductSpecification.isDeleted())
                .and(ProductSpecification.containIds(ids));

        return productRepository.findAll(spec)
                .stream().map(productMapper::toResponse).toList();
    }
}
