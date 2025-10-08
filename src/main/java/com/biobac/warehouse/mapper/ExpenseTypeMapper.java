package com.biobac.warehouse.mapper;

import com.biobac.warehouse.entity.ExpenseType;
import com.biobac.warehouse.response.ExpenseTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseTypeMapper {
    ExpenseTypeResponse toResponse(ExpenseType entity);
}
