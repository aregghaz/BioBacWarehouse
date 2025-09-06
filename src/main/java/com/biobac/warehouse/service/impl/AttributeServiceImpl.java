package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AttributeDefinitionMapper;
import com.biobac.warehouse.repository.AttributeDefinitionRepository;
import com.biobac.warehouse.repository.AttributeGroupRepository;
import com.biobac.warehouse.repository.AttributeValueRepository;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.request.FilterCriteria;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.response.AttributeValueResponse;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.utils.AttributeValueUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeValueRepository valueRepository;
    private final AttributeGroupRepository attributeGroupRepository;
    private final AttributeDefinitionMapper attributeDefinitionMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "desc";

    private Pageable buildPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size <= 0 ? DEFAULT_SIZE : size;
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        String sd = (sortDir == null || sortDir.isBlank()) ? DEFAULT_SORT_DIR : sortDir;
        Sort sort = sd.equalsIgnoreCase("asc") ? org.springframework.data.domain.Sort.by(safeSortBy).ascending() : org.springframework.data.domain.Sort.by(safeSortBy).descending();
        if (safeSize > 1000) {
            safeSize = 1000;
        }
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<AttributeDataType> getAttributeDataTypes() {
        return EnumSet.allOf(AttributeDataType.class);
    }

    @Override
    public AttributeDefResponse createAttributeDefinition(AttributeDefRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new InvalidDataException("Attribute name is required");
        }
        if (request.getDataType() == null) {
            throw new InvalidDataException("Attribute dataType is required");
        }

        List<AttributeGroup> groups = Collections.emptyList();
        if (request.getAttributeGroupIds() != null && !request.getAttributeGroupIds().isEmpty()) {
            groups = attributeGroupRepository.findAllById(request.getAttributeGroupIds());
            if (groups.size() != request.getAttributeGroupIds().size()) {
                throw new NotFoundException("One or more attribute groups not found");
            }
        }

        Optional<AttributeDefinition> existingOpt = definitionRepository.findByNameAndDeletedFalse(request.getName());
        AttributeDefinition def;
        if (existingOpt.isPresent()) {
            def = existingOpt.get();
            if (def.getDataType() != request.getDataType()) {
                throw new InvalidDataException("Attribute '" + request.getName() + "' already exists with different data type");
            }
            if (!groups.isEmpty()) {
                boolean changed = false;
                for (AttributeGroup g : groups) {
                    if (!def.getGroups().contains(g)) {
                        def.getGroups().add(g);
                        changed = true;
                    }
                }
                if (changed) {
                    def = definitionRepository.save(def);
                }
            }
        } else {
            def = new AttributeDefinition();
            def.setName(request.getName());
            def.setDataType(request.getDataType());
            if (!groups.isEmpty()) {
                def.getGroups().addAll(groups);
            }
            def = definitionRepository.save(def);
        }
        AttributeDefResponse r = new AttributeDefResponse();
        r.setName(def.getName());
        r.setDataType(def.getDataType());
        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getDefinitionsByGroups(List<Long> attributeGroupIds) {
        List<AttributeDefinition> defs;
        if (attributeGroupIds == null || attributeGroupIds.isEmpty()) {
            defs = definitionRepository.findByDeletedFalse();
        } else {
            defs = definitionRepository.findDistinctByGroups_IdInAndDeletedFalse(attributeGroupIds);
        }
        return defs.stream().map(attributeDefinitionMapper::toDto).toList();
    }

    @Override
    public Pair<List<AttributeDefResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<AttributeDefinition> attributeDefinitionPage = definitionRepository.findAll(pageable);

        List<AttributeDefResponse> content = attributeDefinitionPage.getContent().stream()
                .map(attributeDefinitionMapper::toDto)
                .collect(Collectors.toList());

        PaginationMetadata metadata = new PaginationMetadata(
                attributeDefinitionPage.getNumber(),
                attributeDefinitionPage.getSize(),
                attributeDefinitionPage.getTotalElements(),
                attributeDefinitionPage.getTotalPages(),
                attributeDefinitionPage.isLast(),
                filters,
                pageable.getSort().toString().contains("ASC") ? "asc" : "desc",
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                "attributeDefinitionTable"
        );

        return Pair.of(content, metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getValuesForIngredient(Long ingredientId) {
        if (ingredientId == null) {
            throw new InvalidDataException("ingredientId is required");
        }
        List<AttributeValue> values = valueRepository.findByIngredient_IdAndDeletedFalse(ingredientId);
        return values.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void createValuesForIngredient(Ingredient ingredient, List<AttributeUpsertRequest> attributes) {
        if (ingredient == null || ingredient.getId() == null) {
            throw new InvalidDataException("Ingredient must be persisted before assigning attributes");
        }
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        List<AttributeValue> toSave = new ArrayList<>();
        for (AttributeUpsertRequest req : attributes) {
            validateReq(req);
            AttributeDefinition def = findOrCreateDefinition(req.getName(), req.getDataType());
            AttributeValue v = valueRepository
                    .findByDefinition_IdAndIngredient_Id(def.getId(), ingredient.getId())
                    .orElseGet(() -> {
                        AttributeValue nv = new AttributeValue();
                        nv.setDefinition(def);
                        nv.setTargetType(AttributeTargetType.INGREDIENT);
                        nv.setIngredient(ingredient);
                        return nv;
                    });
            applyValue(v, req);
            v.setDeleted(false);
            toSave.add(v);
        }
        if (!toSave.isEmpty()) {
            valueRepository.saveAll(toSave);
        }
    }

    private void validateReq(AttributeUpsertRequest req) {
        if (req == null || req.getName() == null || req.getName().isBlank()) {
            throw new InvalidDataException("Attribute name is required");
        }
        if (req.getDataType() == null) {
            throw new InvalidDataException("Attribute dataType is required");
        }
        AttributeValueUtil.validateOrThrow(req.getDataType(), req.getValue());
    }

    private AttributeDefinition findOrCreateDefinition(String name, AttributeDataType type) {
        return definitionRepository.findByNameAndDeletedFalse(name)
                .map(def -> {
                    if (def.getDataType() != type) {
                        throw new InvalidDataException("Attribute '" + name + "' already exists with different data type");
                    }
                    return def;
                })
                .orElseGet(() -> {
                    AttributeDefinition def = new AttributeDefinition();
                    def.setName(name);
                    def.setDataType(type);
                    return definitionRepository.save(def);
                });
    }

    private void applyValue(AttributeValue v, AttributeUpsertRequest req) {
        v.setValue(req.getValue());
    }

    private AttributeDefResponse toResponse(AttributeValue v) {
        AttributeValueResponse r = new AttributeValueResponse();
        r.setName(v.getDefinition().getName());
        r.setDataType(v.getDefinition().getDataType());
        r.setValue(v.getValue());
        if (v.getDefinition().getDataType() != null && AttributeValueUtil.isValid(v.getDefinition().getDataType(), v.getValue())) {
            r.setParsedValue(AttributeValueUtil.parse(v.getDefinition().getDataType(), v.getValue()));
        } else {
            r.setParsedValue(null);
        }
        return r;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getValuesForProduct(Long productId) {
        if (productId == null) {
            throw new InvalidDataException("productId is required");
        }
        List<AttributeValue> values = valueRepository.findByProduct_IdAndDeletedFalse(productId);
        return values.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void createValuesForProduct(Product product, List<AttributeUpsertRequest> attributes) {
        if (product == null || product.getId() == null) {
            throw new InvalidDataException("Product must be persisted before assigning attributes");
        }
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        List<AttributeValue> toSave = new ArrayList<>();
        for (AttributeUpsertRequest req : attributes) {
            validateReq(req);
            AttributeDefinition def = findOrCreateDefinition(req.getName(), req.getDataType());
            AttributeValue v = valueRepository
                    .findByDefinition_IdAndProduct_Id(def.getId(), product.getId())
                    .orElseGet(() -> {
                        AttributeValue nv = new AttributeValue();
                        nv.setDefinition(def);
                        nv.setTargetType(AttributeTargetType.PRODUCT);
                        nv.setProduct(product);
                        return nv;
                    });
            applyValue(v, req);
            v.setDeleted(false);
            toSave.add(v);
        }
        if (!toSave.isEmpty()) {
            valueRepository.saveAll(toSave);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefResponse> getValuesForWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new InvalidDataException("warehouseId is required");
        }
        List<AttributeValue> values = valueRepository.findByWarehouse_IdAndDeletedFalse(warehouseId);
        return values.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void createValuesForWarehouse(Warehouse warehouse, List<AttributeUpsertRequest> attributes) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new InvalidDataException("Warehouse must be persisted before assigning attributes");
        }
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        List<AttributeValue> toSave = new ArrayList<>();
        for (AttributeUpsertRequest req : attributes) {
            validateReq(req);
            AttributeDefinition def = findOrCreateDefinition(req.getName(), req.getDataType());
            AttributeValue v = valueRepository
                    .findByDefinition_IdAndWarehouse_Id(def.getId(), warehouse.getId())
                    .orElseGet(() -> {
                        AttributeValue nv = new AttributeValue();
                        nv.setDefinition(def);
                        nv.setTargetType(AttributeTargetType.WAREHOUSE);
                        nv.setWarehouse(warehouse);
                        return nv;
                    });
            applyValue(v, req);
            v.setDeleted(false);
            toSave.add(v);
        }
        if (!toSave.isEmpty()) {
            valueRepository.saveAll(toSave);
        }
    }
}
