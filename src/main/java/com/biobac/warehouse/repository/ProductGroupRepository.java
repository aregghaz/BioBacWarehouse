package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long>, JpaSpecificationExecutor<ProductGroup> {
}
