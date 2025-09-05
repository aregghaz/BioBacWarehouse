package com.biobac.warehouse.service.impl;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.AttributeGroup;
import com.biobac.warehouse.exception.NotFoundException;
import com.biobac.warehouse.mapper.AttributeGroupMapper;
import com.biobac.warehouse.repository.AttributeDefinitionRepository;
import com.biobac.warehouse.repository.AttributeGroupRepository;
import com.biobac.warehouse.request.AttributeGroupCreateRequest;
import com.biobac.warehouse.response.AttributeGroupResponse;
import com.biobac.warehouse.service.AttributeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeGroupServiceImpl implements AttributeGroupService {

    private final AttributeGroupRepository attributeGroupRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeGroupMapper mapper;

    @Override
    public List<AttributeGroupResponse> getAll() {
        return attributeGroupRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
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

        // Persist the group first to obtain an ID
        AttributeGroup savedGroup = attributeGroupRepository.save(entity);

        // If attributes provided, update the owning side (AttributeDefinition.groups)
        if (group.getAttributeIds() != null && !group.getAttributeIds().isEmpty()) {
            Set<AttributeDefinition> attributeDefinitions = new HashSet<>(attributeDefinitionRepository.findAllById(group.getAttributeIds()));
            for (AttributeDefinition def : attributeDefinitions) {
                def.getGroups().add(savedGroup);
            }
            attributeDefinitionRepository.saveAll(attributeDefinitions);
            // Also reflect on the inverse side for completeness
            savedGroup.setDefinitions(attributeDefinitions);
            savedGroup = attributeGroupRepository.save(savedGroup);
        }
        return mapper.toDto(savedGroup);
    }

    @Override
    public AttributeGroupResponse update(Long id, AttributeGroupCreateRequest group) {
        AttributeGroup existing = attributeGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("AttributeGroup not found with id: " + id));

        // Update basic fields
        mapper.update(existing, group);
        AttributeGroup savedGroup = attributeGroupRepository.save(existing);

        // If attributeIds provided, sync relations on owning side (AttributeDefinition)
        if (group.getAttributeIds() != null) {
            Set<AttributeDefinition> newDefs = new HashSet<>();
            if (!group.getAttributeIds().isEmpty()) {
                newDefs = new HashSet<>(attributeDefinitionRepository.findAllById(group.getAttributeIds()));
            }
            Set<AttributeDefinition> currentDefs = new HashSet<>(savedGroup.getDefinitions());

            // Definitions to remove
            Set<AttributeDefinition> toRemove = new HashSet<>(currentDefs);
            toRemove.removeAll(newDefs);
            for (AttributeDefinition def : toRemove) {
                def.getGroups().remove(savedGroup);
            }

            // Definitions to add
            Set<AttributeDefinition> toAdd = new HashSet<>(newDefs);
            toAdd.removeAll(currentDefs);
            for (AttributeDefinition def : toAdd) {
                def.getGroups().add(savedGroup);
            }

            // Persist owning-side changes
            Set<AttributeDefinition> changed = new HashSet<>();
            changed.addAll(toRemove);
            changed.addAll(toAdd);
            if (!changed.isEmpty()) {
                attributeDefinitionRepository.saveAll(changed);
            }

            // Reflect on inverse side and save group
            savedGroup.setDefinitions(newDefs);
            savedGroup = attributeGroupRepository.save(savedGroup);
        }

        return mapper.toDto(savedGroup);
    }

    @Override
    public void delete(Long id) {
        if (!attributeGroupRepository.existsById(id)) {
            throw new NotFoundException("AttributeGroup not found with id: " + id);
        }
        attributeGroupRepository.deleteById(id);
    }
}
