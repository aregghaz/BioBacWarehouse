package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttributeGroupRepository extends JpaRepository<AttributeGroup, Long> {
    @Query("SELECT g FROM AttributeGroup g LEFT JOIN FETCH g.definitions WHERE g.id = :id")
    Optional<AttributeGroup> findByIdWithDefinitions(@Param("id") Long id);
}
