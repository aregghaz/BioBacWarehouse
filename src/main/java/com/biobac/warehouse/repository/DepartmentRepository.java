package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
}
