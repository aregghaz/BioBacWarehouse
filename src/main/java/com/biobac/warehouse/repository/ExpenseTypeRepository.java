package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long>, JpaSpecificationExecutor<ExpenseType> {
}
