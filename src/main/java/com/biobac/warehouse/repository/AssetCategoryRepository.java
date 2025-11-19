package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long>, JpaSpecificationExecutor<AssetCategory> {
}