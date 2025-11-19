package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ReceiveExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiveExpenseRepository extends JpaRepository<ReceiveExpense, Long> {
    List<ReceiveExpense> findByGroupId(Long groupId);
    void deleteByGroupId(Long groupId);
}
