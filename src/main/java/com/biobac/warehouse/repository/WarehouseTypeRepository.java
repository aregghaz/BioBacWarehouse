package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.WarehouseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseTypeRepository extends JpaRepository<WarehouseType, Long>, JpaSpecificationExecutor<WarehouseType> {
}
