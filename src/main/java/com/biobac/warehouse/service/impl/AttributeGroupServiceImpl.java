package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AttributeGroupMapper;
import com.biobac.warehouse.repository.AttributeDefinitionRepository;
import com.biobac.warehouse.repository.AttributeGroupRepository;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AttributeGroupResponse;
import com.biobac.warehouse.service.AttributeGroupService;
import com.biobac.warehouse.utils.specifications.AttributeGroupSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeGroupServiceImpl implements AttributeGroupService {

    private final AttributeGroupRepository attributeGroupRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeGroupMapper mapper;

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
    public List<AttributeGroupResponse> getAll() {
        return attributeGroupRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<AttributeGroupResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters,
                                                                                Integer page,
                                                                                Integer size,
                                                                                String sortBy,
                                                                                String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<AttributeGroup> spec = AttributeGroupSpecification.buildSpecification(filters);
        Page<AttributeGroup> groupPage = attributeGroupRepository.findAll(spec, pageable);
        List<AttributeGroupResponse> content = groupPage.getContent().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                groupPage.getNumber(),
                groupPage.getSize(),
                groupPage.getTotalElements(),
                groupPage.getTotalPages(),
                groupPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "attributeGroupTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    public AttributeGroupResponse getById(Long id) {
        AttributeGroup entity = attributeGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AttributeGroup not found with id: " + id));
        return mapper.toDto(entity);
    }

    @Override
    public AttributeGroupResponse create(AttributeGroupCreateRequest group) {
        AttributeGroup entity = mapper.toEntity(group);

        AttributeGroup savedGroup = attributeGroupRepository.save(entity);

        if (group.getAttributeIds() != null && !group.getAttributeIds().isEmpty()) {
            Set<AttributeDefinition> attributeDefinitions = new HashSet<>(attributeDefinitionRepository.findAllById(group.getAttributeIds()));
            for (AttributeDefinition def : attributeDefinitions) {
                def.getGroups().add(savedGroup);
            }
            attributeDefinitionRepository.saveAll(attributeDefinitions);

            savedGroup.setDefinitions(attributeDefinitions);
            savedGroup = attributeGroupRepository.save(savedGroup);
        }
        return mapper.toDto(savedGroup);
    }

    @Override
    public AttributeGroupResponse update(Long id, AttributeGroupCreateRequest group) {
        AttributeGroup existing = attributeGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AttributeGroup not found with id: " + id));

        mapper.update(existing, group);
        AttributeGroup savedGroup = attributeGroupRepository.save(existing);

        if (group.getAttributeIds() != null) {
            Set<AttributeDefinition> newDefs = new HashSet<>();
            if (!group.getAttributeIds().isEmpty()) {
                newDefs = new HashSet<>(attributeDefinitionRepository.findAllById(group.getAttributeIds()));
            }
            Set<AttributeDefinition> currentDefs = new HashSet<>(savedGroup.getDefinitions());

            Set<AttributeDefinition> toRemove = new HashSet<>(currentDefs);
            toRemove.removeAll(newDefs);
            for (AttributeDefinition def : toRemove) {
                def.getGroups().remove(savedGroup);
            }

            Set<AttributeDefinition> toAdd = new HashSet<>(newDefs);
            toAdd.removeAll(currentDefs);
            for (AttributeDefinition def : toAdd) {
                def.getGroups().add(savedGroup);
            }

            Set<AttributeDefinition> changed = new HashSet<>();
            changed.addAll(toRemove);
            changed.addAll(toAdd);
            if (!changed.isEmpty()) {
                attributeDefinitionRepository.saveAll(changed);
            }

            savedGroup.setDefinitions(newDefs);
            savedGroup = attributeGroupRepository.save(savedGroup);
        }

        return mapper.toDto(savedGroup);
    }

    @Override
    public void delete(Long id) {
        AttributeGroup group = attributeGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute Group not found with id: " + id));

        for (AttributeDefinition def : group.getDefinitions()) {
            def.getGroups().remove(group);
        }

        group.getDefinitions().clear();

        attributeGroupRepository.delete(group);
    }
}
