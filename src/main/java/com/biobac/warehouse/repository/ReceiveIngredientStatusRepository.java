package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ReceiveIngredientStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceiveIngredientStatusRepository extends JpaRepository<ReceiveIngredientStatus, Long> {
    Optional<ReceiveIngredientStatus> findByName(String name);
}
