package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.IngredientBalance;
import com.biobac.warehouse.entity.IngredientDetail;
import com.biobac.warehouse.entity.ProductBalance;
import com.biobac.warehouse.entity.ProductDetail;
import com.biobac.warehouse.mapper.ComponentBalanceMapper;
import com.biobac.warehouse.repository.IngredientBalanceRepository;
import com.biobac.warehouse.repository.IngredientDetailRepository;
import com.biobac.warehouse.repository.ProductBalanceRepository;
import com.biobac.warehouse.repository.ProductDetailRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ComponentBalanceIngResponse;
import com.biobac.warehouse.response.ComponentBalanceProdResponse;
import com.biobac.warehouse.response.IngredientDetailResponse;
import com.biobac.warehouse.response.ProductDetailResponse;
import com.biobac.warehouse.service.ComponentBalanceService;
import com.biobac.warehouse.utils.specifications.IngredientBalanceSpecification;
import com.biobac.warehouse.utils.specifications.IngredientDetailSpecification;
import com.biobac.warehouse.utils.specifications.ProductBalanceSpecification;
import com.biobac.warehouse.utils.specifications.ProductDetailSpecification;
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
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ComponentBalanceIngResponse>, PaginationMetadata> getIngPagination(Map<String, FilterCriteria> filters,
                                                                                        Integer page,
                                                                                        Integer size,
                                                                                        String sortBy,
                                                                                        String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<IngredientBalance> spec = IngredientBalanceSpecification.buildSpecification(filters)
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
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ProductBalance> spec = ProductBalanceSpecification.buildSpecification(filters)
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
                    response.setProductName(c.getProductBalance().getProduct().getName());
                    response.setExpirationDate(c.getExpirationDate());
                    response.setQuantity(c.getQuantity());
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
    public Pair<List<IngredientDetailResponse>, PaginationMetadata> getIngredientDetailsByProductId(Long id, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<IngredientDetail> spec = IngredientDetailSpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredientBalance", JoinType.LEFT).get("id").in(id));
        Page<IngredientDetail> ingredientDetailPage = ingredientDetailRepository.findAll(spec, pageable);

        List<IngredientDetailResponse> content = ingredientDetailPage.getContent()
                .stream()
                .map(c -> {
                    IngredientDetailResponse response = new IngredientDetailResponse();
                    response.setIngredientName(c.getIngredientBalance().getIngredient().getName());
                    response.setExpirationDate(c.getExpirationDate());
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
}
