package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnitTypeRepository extends JpaRepository<UnitType, Long>, JpaSpecificationExecutor<UnitType> {
}
