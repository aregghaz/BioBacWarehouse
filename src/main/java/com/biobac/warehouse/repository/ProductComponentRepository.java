package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductComponentRepository extends JpaRepository<ProductComponent, Long>, JpaSpecificationExecutor<ProductComponent> {
}
