package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.IngredientHistoryDto;
import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.entity.IngredientHistory;
import com.biobac.warehouse.mapper.IngredientHistoryMapper;
import com.biobac.warehouse.repository.IngredientHistoryRepository;
import com.biobac.warehouse.request.FilterCriteria;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientHistoryServiceImpl implements IngredientHistoryService {

    private final IngredientHistoryRepository ingredientHistoryRepository;
    private final IngredientHistoryMapper ingredientHistoryMapper;

    @Override
    @Transactional
    public IngredientHistoryDto recordQuantityChange(Ingredient ingredient, Double quantityBefore,
                                                     Double quantityAfter, String action, String notes, BigDecimal lastPrice, Long lastCompanyId) {
        IngredientHistory history = new IngredientHistory();
        history.setIngredient(ingredient);
        history.setAction(action);
        history.setQuantityBefore(quantityBefore);
        history.setQuantityAfter(quantityAfter);
        history.setNotes(notes);

        IngredientHistory savedHistory = ingredientHistoryRepository.save(history);
        return ingredientHistoryMapper.toDto(savedHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistoryDto>, PaginationMetadata> getHistoryForIngredient(Long ingredientId, Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters)
                .and((root, query, cb) -> root.join("ingredient", JoinType.LEFT).get("id").in(ingredientId));
        ;
        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistoryDto> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toDto)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "ingredientHistoryTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<IngredientHistoryDto>, PaginationMetadata> getHistory(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<IngredientHistory> spec = IngredientHistorySpecification.buildSpecification(filters);
        Page<IngredientHistory> ingredientHistoryPage = ingredientHistoryRepository.findAll(spec, pageable);

        List<IngredientHistoryDto> content = ingredientHistoryPage.getContent()
                .stream()
                .map(ingredientHistoryMapper::toDto)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                ingredientHistoryPage.getNumber(),
                ingredientHistoryPage.getSize(),
                ingredientHistoryPage.getTotalElements(),
                ingredientHistoryPage.getTotalPages(),
                ingredientHistoryPage.isLast(),
                filters,
                sortDir,
                sortBy,
                "ingredientsHistoryTable"
        );

        return Pair.of(content, metadata);
    }
}