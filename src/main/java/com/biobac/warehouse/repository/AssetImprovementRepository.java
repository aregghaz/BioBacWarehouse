package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AssetImprovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AssetImprovementRepository extends JpaRepository<AssetImprovement, Long>, JpaSpecificationExecutor<AssetImprovement> {
    List<AssetImprovement> findByAssetId(Long assetId);
}
