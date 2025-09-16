package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long>, JpaSpecificationExecutor<AttributeDefinition> {
    Optional<AttributeDefinition> findByNameAndDeletedFalse(String name);
    List<AttributeDefinition> findDistinctByGroups_IdInAndDeletedFalse(Iterable<Long> groupIds);

    List<AttributeDefinition> findByDeletedFalse();

    Optional<AttributeDefinition> findByIdAndDeletedFalse(Long id);
}
