package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.mapper.IngredientHistoryMapper;
import com.biobac.warehouse.repository.IngredientBalanceRepository;
import com.biobac.warehouse.repository.IngredientHistoryRepository;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.IngredientHistoryResponse;
import com.biobac.warehouse.response.IngredientHistorySingleResponse;
import com.biobac.warehouse.service.IngredientHistoryService;
import com.biobac.warehouse.utils.specifications.IngredientHistorySpecification;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientHistoryServiceImpl implements IngredientHistoryService {

    private final IngredientHistoryRepository ingredientHistoryRepository;
    private final IngredientHistoryMapper ingredientHistoryMapper;
    private final IngredientBalanceRepository ingredientBalanceRepository;

    @Override
    @Transactional
    public IngredientHistorySingleResponse recordQuantityChange(LocalDate timestamp, Ingredient ingredient, Double quantityBefore,
                                                                Double quantityAfter, String notes, BigDecimal lastPrice, Long lastCompanyId) {
        IngredientHistory history = new IngredientHistory();
        history.setIngredient(ingredient);
        boolean increase = (quantityAfter != null ? quantityAfter : 0.0) - (quantityBefore != null ? quantityBefore : 0.0) > 0;
        Double change = (quantityAfter != null ? quantityAfter : 0.0) - (quantityBefore != null ? quantityBefore : 0.0);
        history.setIncrease(increase);
        history.setQuantityChange(change);
        Double total = ingredientBalanceRepository.sumBalanceByIngredientId(ingredient.getId());
        history.setQuantityResult(total != null ? total : 0.0);
        history.setNotes(notes);
        history.setCompanyId(lastCompanyId);
        history.setLastPrice(lastPrice);
        history.setTimestamp(timestamp);

        IngredientHistory savedHistory = ingredientHistoryRepository.save(history);
        return ingredientHistoryMapper.toSingleResponse(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0) ? 20 : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        Sort sort = safeSortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();

        if (!safeSortBy.equalsIgnoreCase("id")) {
            sort = sort.and(Sort.by("id").descending());
        }

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters)
                .and((root, query, cb) -> cb.equal(root.join("ingredient", JoinType.LEFT).get("id"), ingredientId));

        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistorySingleResponse> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toSingleResponse)
                .collect(Collectors.toList());

        String metaSortDir = pageable.getSort().toString().contains("ASC") ? "asc" : "desc";
        String metaSortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                metaSortDir,
                metaSortBy,
                "ingredientHistoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Transactional(readOnly = true)
    @Override
    public List<IngredientHistoryResponse> getAll() {
        Set<Long> ingredientIds = ingredientHistoryRepository.findAll().stream().map(
                i -> i.getIngredient().getId()
        ).collect(Collectors.toSet());

        return ingredientIds.stream().map(i -> {
            IngredientHistory first = ingredientHistoryRepository.findEarliestByIngredientId(i);
            IngredientHistory last = ingredientHistoryRepository.findLatestByIngredientId(i);

            IngredientHistoryResponse response = ingredientHistoryMapper.toResponse(first);
            response.setInitialCount(first.getQuantityResult());
            response.setEventualCount(last.getQuantityResult());
            return response;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistorySingleResponse>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = (page == null || page < 0) ? 0 : page;
        int safeSize = (size == null || size <= 0) ? 20 : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        String safeSortDir = (sortDir == null || sortDir.isBlank()) ? "desc" : sortDir;

        Sort sort = safeSortDir.equalsIgnoreCase("asc") ? Sort.by(safeSortBy).ascending() : Sort.by(safeSortBy).descending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters);
        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistorySingleResponse> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toSingleResponse)
                .collect(Collectors.toList());

        String metaSortDir = pageable.getSort().toString().contains("ASC") ? "asc" : "desc";
        String metaSortBy = pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("id");

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                metaSortDir,
                metaSortBy,
                "ingredientsHistoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalForIngredient(Long ingredientId) {
        Double total = ingredientBalanceRepository.sumBalanceByIngredientId(ingredientId);
        return total != null ? total : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getInitialForIngredient(Long ingredientId, Map<String, FilterCriteria> filters) {
        Specification<IngredientHistory> spec = (root, query, cb) -> cb.equal(root.join("ingredient", JoinType.LEFT).get("id"), ingredientId);
        if (filters != null && !filters.isEmpty()) {
            spec = spec.and(IngredientHistorySpecification.buildSpecification(filters));
        }

        List<IngredientHistory> list = ingredientHistoryRepository.findAll(
                spec, Sort.by("timestamp").ascending().and(Sort.by("id").ascending())
        );
        if (!list.isEmpty()) {
            IngredientHistory first = list.get(0);
            return first.getQuantityResult() != null ? first.getQuantityResult() : 0.0;
        }
        return 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getEventualForIngredient(Long ingredientId, Map<String, FilterCriteria> filters) {
        Specification<IngredientHistory> spec = (root, query, cb) -> cb.equal(root.join("ingredient", JoinType.LEFT).get("id"), ingredientId);
        if (filters != null && !filters.isEmpty()) {
            spec = spec.and(IngredientHistorySpecification.buildSpecification(filters));
        }

        List<IngredientHistory> list = ingredientHistoryRepository.findAll(
                spec,
                Sort.by("timestamp").descending().and(Sort.by("id").descending())
        );
        if (!list.isEmpty()) {
            IngredientHistory last = list.get(0);
            return last.getQuantityResult() != null ? last.getQuantityResult() : 0.0;
        }
        return 0.0;
    }
}