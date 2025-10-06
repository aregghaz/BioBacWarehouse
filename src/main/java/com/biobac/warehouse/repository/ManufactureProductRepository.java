package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ManufactureProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ManufactureProductRepository extends JpaRepository<ManufactureProduct, Long>, JpaSpecificationExecutor<ManufactureProduct> {
}
