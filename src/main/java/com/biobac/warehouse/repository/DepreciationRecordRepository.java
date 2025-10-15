package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.DepreciationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepreciationRecordRepository extends JpaRepository<DepreciationRecord, Long>, JpaSpecificationExecutor<DepreciationRecord> {
}
