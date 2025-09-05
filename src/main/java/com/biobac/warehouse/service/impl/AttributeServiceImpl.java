package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.AttributeDataType;
import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.entity.AttributeTargetType;
import com.biobac.warehouse.entity.AttributeValue;
import com.biobac.warehouse.entity.Ingredient;
import com.biobac.warehouse.exception.InvalidDataException;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.repository.*;
import com.biobac.warehouse.request.AttributeDefRequest;
import com.biobac.warehouse.request.AttributeUpsertRequest;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.response.AttributeValueResponse;
import com.biobac.warehouse.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {

    private final AttributeDefinitionRepository definitionRepository;
    private final AttributeValueRepository valueRepository;
    private final ProductRepository productRepository;
    private final ProductGroupRepository productGroupRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository ingredientGroupRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseGroupRepository warehouseGroupRepository;
    private final AttributeGroupRepository attributeGroupRepository;

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
        if (attributeGroupIds == null || attributeGroupIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<AttributeDefinition> defs = definitionRepository.findDistinctByGroups_IdInAndDeletedFalse(attributeGroupIds);
        return defs.stream().map(def -> {
            AttributeDefResponse r = new AttributeDefResponse();
            r.setName(def.getName());
            r.setDataType(def.getDataType());
            return r;
        }).toList();
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
        // Store all values as string for now; parsing/conversion can be added in the future
        v.setValue(req.getValue());
    }

    private AttributeDefResponse toResponse(AttributeValue v) {
        AttributeValueResponse r = new AttributeValueResponse();
        r.setName(v.getDefinition().getName());
        r.setDataType(v.getDefinition().getDataType());
        r.setValue(v.getValue());
        if (v.getProduct() != null) {
            r.setSourceLevel("PRODUCT");
            r.setSourceId(v.getProduct().getId());
        } else if (v.getIngredient() != null) {
            r.setSourceLevel("INGREDIENT");
            r.setSourceId(v.getIngredient().getId());
        } else if (v.getWarehouse() != null) {
            r.setSourceLevel("WAREHOUSE");
            r.setSourceId(v.getWarehouse().getId());
        }
        return r;
    }
}
