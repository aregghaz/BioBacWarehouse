package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.HistoryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface HistoryActionRepository extends JpaRepository<HistoryAction, Long>, JpaSpecificationExecutor<HistoryAction> {
    Optional<HistoryAction> findByName(String name);
}
