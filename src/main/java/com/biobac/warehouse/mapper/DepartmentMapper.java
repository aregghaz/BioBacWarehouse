package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.Department;
import com.biobac.warehouse.request.DepartmentRequest;
import com.biobac.warehouse.response.DepartmentResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {
    DepartmentResponse toResponse(Department department);

    Department toEntity(DepartmentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(DepartmentRequest request, @MappingTarget Department department);
}
