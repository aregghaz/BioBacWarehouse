package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.DepreciationMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepreciationMethodRepository extends JpaRepository<DepreciationMethod, Long>, JpaSpecificationExecutor<DepreciationMethod> {
}
