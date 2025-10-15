package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
}
