package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.dto.ProductHistoryDto;
import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductHistory;
import com.biobac.warehouse.mapper.ProductHistoryMapper;
import com.biobac.warehouse.repository.ProductHistoryRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.service.ProductHistoryService;
import com.biobac.warehouse.utils.GroupUtil;
import com.biobac.warehouse.utils.specifications.ProductHistorySpecification;
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
public class ProductHistoryServiceImpl implements ProductHistoryService {

    private final ProductHistoryRepository productHistoryRepository;
    private final ProductHistoryMapper productHistoryMapper;
    private final GroupUtil groupUtil;

    @Override
    @Transactional
    public ProductHistoryDto recordQuantityChange(Product product, Double quantityBefore, Double quantityAfter, String action, String notes) {
        ProductHistory history = new ProductHistory();
        history.setProduct(product);
        history.setTimestamp(LocalDateTime.now());
        history.setAction(action);
        history.setQuantityBefore(quantityBefore);
        history.setQuantityAfter(quantityAfter);
        history.setNotes(notes);

        ProductHistory saved = productHistoryRepository.save(history);
        return productHistoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductHistoryDto>, PaginationMetadata> getHistoryForProduct(Long productId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<ProductHistory> spec = ProductHistorySpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("product", JoinType.LEFT).get("id").in(productId));
        Page<ProductHistory> pageResult = productHistoryRepository.findAll(spec, pageable);

        List<ProductHistoryDto> content = pageResult.getContent()
                .stream()
                .map(productHistoryMapper::toDto)
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
                "productHistoryTable"
        );
        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ProductHistoryDto>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        List<Long> productGroupIds = groupUtil.getAccessibleProductGroupIds();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<ProductHistory> spec = ProductHistorySpecification.buildSpecification(filters)
                .and(ProductHistorySpecification.belongsToProductGroups(productGroupIds));
        Page<ProductHistory> pageResult = productHistoryRepository.findAll(spec, pageable);

        List<ProductHistoryDto> content = pageResult.getContent()
                .stream()
                .map(productHistoryMapper::toDto)
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
                "productsHistoryTable"
        );
        return Pair.of(content, metadata);
    }
}