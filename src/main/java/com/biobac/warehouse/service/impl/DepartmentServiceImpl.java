package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.Department;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.DepartmentMapper;
import com.biobac.warehouse.repository.DepartmentRepository;
import com.biobac.warehouse.request.DepartmentRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.DepartmentResponse;
import com.biobac.warehouse.service.DepartmentService;
import com.biobac.warehouse.utils.specifications.DepartmentSpecification;
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
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentMapper departmentMapper;
    private final DepartmentRepository departmentRepository;

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
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = departmentMapper.toEntity(request);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));

        departmentMapper.updateEntityFromRequest(request, existing);

        Department saved = departmentRepository.save(existing);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        departmentRepository.delete(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(departmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<DepartmentResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Department> spec = DepartmentSpecification.buildSpecification(filters);
        Page<Department> pageResult = departmentRepository.findAll(spec, pageable);

        List<DepartmentResponse> content = pageResult.getContent().stream()
                .map(departmentMapper::toResponse)
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
                "departmentTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
        return departmentMapper.toResponse(department);
    }
}
