package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.ExpenseType;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.ExpenseTypeMapper;
import com.biobac.warehouse.repository.ExpenseTypeRepository;
import com.biobac.warehouse.request.ExpenseTypeCreateRequest;
import com.biobac.warehouse.request.ExpenseTypeUpdateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.ExpenseTypeResponse;
import com.biobac.warehouse.service.ExpenseTypeService;
import com.biobac.warehouse.utils.specifications.ExpenseTypeSpecification;
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
public class ExpenseTypeServiceImpl implements ExpenseTypeService {
    private final ExpenseTypeRepository expenseTypeRepository;
    private final ExpenseTypeMapper expenseTypeMapper;

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
    public ExpenseTypeResponse getById(Long id) {
        ExpenseType entity = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExpenseType not found with id: " + id));
        return expenseTypeMapper.toResponse(entity);
    }

    @Override
    public ExpenseType getExpenseTypeById(Long id) {
        return expenseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExpenseType not found with id: " + id));
    }

    @Override
    @Transactional
    public ExpenseTypeResponse create(ExpenseTypeCreateRequest request) {
        ExpenseType entity = new ExpenseType();
        entity.setName(request.getName());
        ExpenseType saved = expenseTypeRepository.save(entity);
        return expenseTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExpenseTypeResponse update(ExpenseTypeUpdateRequest request) {
        ExpenseType existing = expenseTypeRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("ExpenseType not found with id: " + request.getId()));
        existing.setName(request.getName());
        ExpenseType saved = expenseTypeRepository.save(existing);
        return expenseTypeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseTypeResponse> getAll() {
        return expenseTypeRepository.findAll().stream()
                .map(expenseTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<ExpenseTypeResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<ExpenseType> spec = ExpenseTypeSpecification.buildSpecification(filters);
        Page<ExpenseType> pageResult = expenseTypeRepository.findAll(spec, pageable);

        List<ExpenseTypeResponse> content = pageResult.getContent().stream()
                .map(expenseTypeMapper::toResponse)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "expenseTypeTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ExpenseType existing = expenseTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExpenseType not found with id: " + id));
        expenseTypeRepository.delete(existing);
    }
}
