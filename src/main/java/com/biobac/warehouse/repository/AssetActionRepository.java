package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AssetAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetActionRepository extends JpaRepository<AssetAction, Long>, JpaSpecificationExecutor<AssetAction> {
}
