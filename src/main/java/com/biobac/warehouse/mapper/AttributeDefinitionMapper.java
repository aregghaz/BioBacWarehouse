package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.OptionValue;
import com.biobac.warehouse.response.AttributeDefResponse;
import com.biobac.warehouse.response.OptionValueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface AttributeDefinitionMapper {

    @Mapping(target = "attributeGroupIds", expression = "java(mapGroupIds(entity))")
    AttributeDefResponse toDto(AttributeDefinition entity);

    OptionValueResponse toDto(OptionValue option);

    default List<Long> mapGroupIds(AttributeDefinition entity) {
        if (entity == null || entity.getGroups() == null || entity.getGroups().isEmpty()) {
            return List.of();
        }
        return entity.getGroups().stream()
                .filter(Objects::nonNull)
                .map(g -> g.getId())
                .filter(Objects::nonNull)
                .sorted()
                .toList();
    }
}
