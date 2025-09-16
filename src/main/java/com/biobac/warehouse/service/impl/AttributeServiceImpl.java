package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.dto.PaginationMetadata;
import com.biobac.warehouse.entity.*;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AttributeDefinitionMapper;
import com.biobac.warehouse.repository.AttributeDefinitionRepository;
import com.biobac.warehouse.repository.AttributeGroupRepository;
import com.biobac.warehouse.repository.AttributeValueRepository;
import com.biobac.warehouse.repository.OptionValueRepository;
import com.biobac.warehouse.request.*;
import com.biobac.warehouse.response.*;
import com.biobac.warehouse.service.AttributeService;
import com.biobac.warehouse.utils.AttributeValueUtil;
import com.biobac.warehouse.utils.specifications.AttributeSpecification;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeValueRepository valueRepository;
    private final AttributeGroupRepository attributeGroupRepository;
    private final AttributeDefinitionMapper attributeDefinitionMapper;
    private final OptionValueRepository optionValueRepository;

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
    @Transactional
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
            if (def.getDataType() == AttributeDataType.SELECT || def.getDataType() == AttributeDataType.MULTISELECT) {
                if (request.getOptions() != null) {
                    applyOptions(def, request.getOptions());
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
            if (def.getDataType() == AttributeDataType.SELECT || def.getDataType() == AttributeDataType.MULTISELECT) {
                if (request.getOptions() == null || request.getOptions().isEmpty()) {
                    throw new InvalidDataException("Options are required for SELECT/MULTISELECT attribute definitions");
                }
                def = definitionRepository.save(def);
                applyOptions(def, request.getOptions());
            }
            def = definitionRepository.save(def);
        }
        return toDefinitionResponse(def);
    }

    @Override
    @Transactional
    public AttributeDefResponse updateAttributeDefinition(Long id, AttributeDefUpdateRequest request) {
        AttributeDefinition def = definitionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Attribute definition not found"));

        // Load all existing values for this definition to know whether updates are allowed and which options are in use
        List<AttributeValue> existingValues = valueRepository.findAllByDefinition(def);
        boolean hasAnyValues = existingValues != null && !existingValues.isEmpty();

        // Disallow changing data type if any values exist
        if (request.getDataType() != null && request.getDataType() != def.getDataType()) {
            if (hasAnyValues) {
                throw new InvalidDataException("Can't change data type: existing values present for this attribute");
            }
            def.setDataType(request.getDataType());
        }

        if (request.getName() != null) {
            def.setName(request.getName());
        }

        if (request.getAttributeGroupIds() != null) {
            List<AttributeGroup> groups = Collections.emptyList();
            if (!request.getAttributeGroupIds().isEmpty()) {
                groups = attributeGroupRepository.findAllById(request.getAttributeGroupIds());
                if (groups.size() != request.getAttributeGroupIds().size()) {
                    throw new NotFoundException("One or more attribute groups not found");
                }
            }
            def.getGroups().clear();
            if (!groups.isEmpty()) {
                def.getGroups().addAll(groups);
            }
        }

        if ((def.getDataType() == AttributeDataType.SELECT || def.getDataType() == AttributeDataType.MULTISELECT)
                && request.getOptions() != null) {

            // Build set of option IDs currently used in values
            Set<Long> usedOptionIds = new HashSet<>();
            if (existingValues != null) {
                for (AttributeValue v : existingValues) {
                    String raw = v.getValue();
                    if (raw == null || raw.isBlank()) continue;
                    try {
                        if (def.getDataType() == AttributeDataType.SELECT) {
                            usedOptionIds.add(Long.valueOf(raw.trim()));
                        } else {
                            usedOptionIds.addAll(AttributeValueUtil.parseMultiSelect(raw));
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            // Index existing options by id
            Map<Long, OptionValue> existingById = new HashMap<>();
            if (def.getOptions() != null) {
                for (OptionValue ov : def.getOptions()) {
                    if (ov != null && ov.getId() != null) {
                        existingById.put(ov.getId(), ov);
                    }
                }
            }

            List<OptionValueUpdateRequest> additions = new ArrayList<>();
            Map<Long, OptionValueUpdateRequest> updates = new HashMap<>();
            for (OptionValueUpdateRequest reqOpt : request.getOptions()) {
                if (reqOpt.getId() == null) {
                    additions.add(reqOpt);
                } else {
                    updates.put(reqOpt.getId(), reqOpt);
                }
            }

            // Determine removals: existing options not present in updates
            List<OptionValue> toRemove = new ArrayList<>();
            for (Map.Entry<Long, OptionValue> e : existingById.entrySet()) {
                Long optId = e.getKey();
                if (!updates.containsKey(optId)) {
                    // Removal requested for this option
                    if (usedOptionIds.contains(optId)) {
                        throw new InvalidDataException("Cannot remove option in use: id=" + optId);
                    }
                    toRemove.add(e.getValue());
                }
            }

            // Apply updates; if option is used and there is a change, throw
            for (Map.Entry<Long, OptionValueUpdateRequest> e : updates.entrySet()) {
                Long optId = e.getKey();
                OptionValue existing = existingById.get(optId);
                if (existing == null) {
                    throw new NotFoundException("Option not found for update: id=" + optId);
                }
                String newLabel = e.getValue().getLabel();
                String newValue = e.getValue().getValue();

                boolean labelChanged = newLabel != null && !newLabel.equals(existing.getLabel());
                boolean valueChanged = newValue != null && !newValue.equals(existing.getValue());
                if ((labelChanged || valueChanged) && usedOptionIds.contains(optId)) {
                    throw new InvalidDataException("Cannot update option in use: id=" + optId);
                }

                if (labelChanged) {
                    if (newLabel.isBlank()) {
                        throw new InvalidDataException("Option label is required");
                    }
                    existing.setLabel(newLabel);
                }
                if (newValue != null) {
                    if (newValue.isBlank()) {
                        // default to label if empty value provided
                        existing.setValue(existing.getLabel());
                    } else {
                        existing.setValue(newValue);
                    }
                } else if (labelChanged) {
                    // if value not provided during label change, mirror label
                    existing.setValue(existing.getLabel());
                }
                optionValueRepository.save(existing);
            }

            // Apply removals
            if (!toRemove.isEmpty()) {
                for (OptionValue ov : toRemove) {
                    def.getOptions().remove(ov);
                }
                optionValueRepository.deleteAll(toRemove);
            }

            // Apply additions
            for (OptionValueUpdateRequest addReq : additions) {
                String label = addReq.getLabel();
                String val = addReq.getValue();
                if (label == null || label.isBlank()) {
                    throw new InvalidDataException("Option label is required");
                }
                if (val == null || val.isBlank()) {
                    val = label;
                }
                OptionValue ov = new OptionValue();
                ov.setLabel(label);
                ov.setValue(val);
                ov.setAttributeDefinition(def);
                optionValueRepository.save(ov);
                def.getOptions().add(ov);
            }
        } else if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            // For non-select types ignore options updates but prevent accidental clear attempts
            throw new InvalidDataException("Options can be updated only for SELECT/MULTISELECT attributes");
        }

        definitionRepository.save(def);
        return toDefinitionResponse(def);
    }

    @Override
    @Transactional
    public void deleteAttributeDefinition(Long id) {
        if (id == null) {
            throw new InvalidDataException("Attribute definition id is required");
        }
        AttributeDefinition def = definitionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attribute definition not found"));
        if (def.getDataType() == AttributeDataType.SELECT || def.getDataType() == AttributeDataType.MULTISELECT) {
            List<OptionValue> values = optionValueRepository.findAllByAttributeDefinition(def);
            optionValueRepository.deleteAll(values);
        }
        def.setDeleted(true);
        definitionRepository.save(def);
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
        return defs.stream().map(def -> {
            AttributeDataType type = def.getDataType();
            if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTISELECT) {
                AttributeOptionDefResponse resp = new AttributeOptionDefResponse();
                resp.setId(def.getId());
                resp.setName(def.getName());
                resp.setDataType(type);
                resp.setAttributeGroupIds(attributeDefinitionMapper.mapGroupIds(def));
                resp.setCreatedAt(def.getCreatedAt());
                resp.setUpdatedAt(def.getUpdatedAt());
                List<OptionValueResponse> options = def.getOptions() == null ? List.of() : def.getOptions().stream()
                        .filter(Objects::nonNull)
                        .map(attributeDefinitionMapper::toDto)
                        .toList();
                resp.setOptions(options);
                return resp;
            }
            return attributeDefinitionMapper.toDto(def);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AttributeDefResponse>, PaginationMetadata> getPagination(Map<String, FilterCriteria> filters, Integer page, Integer size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Specification<AttributeDefinition> spec = AttributeSpecification.buildSpecification(filters);

        Page<AttributeDefinition> attributeDefinitionPage = definitionRepository.findAll(spec, pageable);

        List<AttributeDefResponse> content = attributeDefinitionPage.getContent().stream().filter(a -> !a.isDeleted())
                .map(this::toDefinitionResponse)
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
    @Transactional(readOnly = true)
    public AttributeDefResponse getById(Long id) {
        AttributeDefinition attributeDefinition = definitionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Attribute not found"));
        return toDefinitionResponse(attributeDefinition);
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

    @Override
    @Transactional
    public void deleteValuesForIngredient(Long ingredientId) {
        if (ingredientId == null) {
            throw new InvalidDataException("ingredientId is required");
        }
        valueRepository.deleteByIngredient_Id(ingredientId);
    }

    @Override
    @Transactional
    public void deleteValuesForProduct(Long productId) {
        if (productId == null) {
            throw new InvalidDataException("productId is required");
        }
        valueRepository.deleteByProduct_Id(productId);
    }

    @Override
    @Transactional
    public void deleteValuesForWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new InvalidDataException("warehouseId is required");
        }
        valueRepository.deleteByWarehouse_Id(warehouseId);
    }

    private void validateReq(AttributeUpsertRequest req) {
        if (req == null || req.getName() == null || req.getName().isBlank()) {
            throw new InvalidDataException("Attribute name is required");
        }
        if (req.getDataType() == null) {
            throw new InvalidDataException("Attribute dataType is required");
        }
        if (req.getDataType() == AttributeDataType.SELECT) {
            if (req.getValues() == null || req.getValues().size() != 1) {
                throw new InvalidDataException("SELECT attribute requires exactly one option id in 'values'");
            }
            return;
        }
        if (req.getDataType() == AttributeDataType.MULTISELECT) {
            if (req.getValues() == null || req.getValues().isEmpty()) {
                throw new InvalidDataException("MULTISELECT attribute requires at least one option id in 'values'");
            }
            return;
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
        AttributeDefinition def = v.getDefinition();
        if (def == null || def.getDataType() == null) {
            v.setValue(req.getValue());
            return;
        }
        AttributeDataType type = def.getDataType();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTISELECT) {
            List<Long> ids = req.getValues();
            req.setValue(null);
            if (ids == null) {
                throw new InvalidDataException("Option ids are required in 'values' for SELECT/MULTISELECT");
            }
            if (type == AttributeDataType.SELECT && ids.size() != 1) {
                throw new InvalidDataException("SELECT attribute requires exactly one option id in 'values'");
            }
            if (type == AttributeDataType.MULTISELECT && ids.isEmpty()) {
                throw new InvalidDataException("MULTISELECT attribute requires at least one option id in 'values'");
            }

            List<OptionValue> options = optionValueRepository.findAllById(ids);
            if (options.size() != ids.size()) {
                throw new NotFoundException("One or more option ids were not found");
            }
            Long defId = def.getId();
            for (OptionValue ov : options) {
                if (ov.getAttributeDefinition() == null || ov.getAttributeDefinition().getId() == null || !ov.getAttributeDefinition().getId().equals(defId)) {
                    throw new InvalidDataException("Provided option id does not belong to the attribute definition: " + ov.getId());
                }
            }
            String stored = type == AttributeDataType.SELECT
                    ? String.valueOf(ids.get(0))
                    : ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            v.setValue(stored);
            return;
        }
        v.setValue(req.getValue());
    }

    private void applyOptions(AttributeDefinition def, List<OptionValueRequest> options) {
        if (def == null) return;
        if (options == null) return;
        def.getOptions().clear();
        for (OptionValueRequest o : options) {
            String label = o.getLabel();
            String value = o.getValue();
            if (label == null || label.isEmpty()) {
                throw new InvalidDataException("Option label is required");
            }
            if (value == null || value.isEmpty()) {
                value = label;
            }
            OptionValue ov = new OptionValue();
            ov.setLabel(label);
            ov.setValue(value);
            ov.setAttributeDefinition(def);
            def.getOptions().add(ov);
            optionValueRepository.save(ov);
        }
    }

    private AttributeDefResponse toDefinitionResponse(AttributeDefinition def) {
        if (def == null) return null;
        AttributeDataType type = def.getDataType();
        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTISELECT) {
            AttributeOptionDefResponse resp = new AttributeOptionDefResponse();
            resp.setId(def.getId());
            resp.setName(def.getName());
            resp.setDataType(type);
            resp.setAttributeGroupIds(attributeDefinitionMapper.mapGroupIds(def));
            resp.setCreatedAt(def.getCreatedAt());
            resp.setUpdatedAt(def.getUpdatedAt());
            List<OptionValueResponse> options = def.getOptions() == null ? List.of() : def.getOptions().stream()
                    .filter(Objects::nonNull)
                    .map(attributeDefinitionMapper::toDto)
                    .toList();
            resp.setOptions(options);
            return resp;
        }
        return attributeDefinitionMapper.toDto(def);
    }

    private AttributeDefResponse toResponse(AttributeValue v) {
        AttributeDataType type = v.getDefinition().getDataType();

        List<Long> groupIds = new ArrayList<>();
        if (!v.getDefinition().getGroups().isEmpty()) {
            for (AttributeGroup attributeGroup : v.getDefinition().getGroups()) {
                groupIds.add(attributeGroup.getId());
            }
        }

        if (type == AttributeDataType.SELECT || type == AttributeDataType.MULTISELECT) {
            List<Long> optionIds = new ArrayList<>();
            String raw = v.getValue();
            if (raw != null && !raw.isBlank()) {
                try {
                    if (type == AttributeDataType.SELECT) {
                        optionIds.add(Long.valueOf(raw.trim()));
                    } else {
                        optionIds.addAll(AttributeValueUtil.parseMultiSelect(raw));
                    }
                } catch (Exception ignored) {
                }
            }
            List<OptionValueResponse> optionResponses = Collections.emptyList();
            if (!optionIds.isEmpty()) {
                List<OptionValue> options = optionValueRepository.findAllById(optionIds);
                optionResponses = options.stream().map(o -> {
                    OptionValueResponse or = new OptionValueResponse();
                    or.setId(o.getId());
                    or.setLabel(o.getLabel());
                    or.setValue(o.getValue());
                    return or;
                }).toList();
            }

            AttributeOptionValueResponse resp = new AttributeOptionValueResponse();
            resp.setId(v.getDefinition().getId());
            resp.setCreatedAt(v.getCreatedAt());
            resp.setUpdatedAt(v.getUpdatedAt());
            resp.setName(v.getDefinition().getName());
            resp.setDataType(type);
            resp.setAttributeGroupIds(groupIds);
            resp.setValue(optionResponses);
            return resp;
        } else {
            AttributeValueResponse resp = new AttributeValueResponse();
            resp.setId(v.getDefinition().getId());
            resp.setCreatedAt(v.getCreatedAt());
            resp.setUpdatedAt(v.getUpdatedAt());
            resp.setName(v.getDefinition().getName());
            resp.setDataType(type);
            if (type != null && AttributeValueUtil.isValid(type, v.getValue())) {
                resp.setValue(AttributeValueUtil.parse(type, v.getValue()));
            } else {
                resp.setValue(null);
            }
            resp.setAttributeGroupIds(groupIds);
            return resp;
        }
    }
}
