package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetStatusRepository extends JpaRepository<AssetStatus, Long>, JpaSpecificationExecutor<AssetStatus> {
}
