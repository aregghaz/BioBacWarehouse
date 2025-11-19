package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.WarehouseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WarehouseGroupRepository extends JpaRepository<WarehouseGroup, Long>, JpaSpecificationExecutor<WarehouseGroup> {
}
