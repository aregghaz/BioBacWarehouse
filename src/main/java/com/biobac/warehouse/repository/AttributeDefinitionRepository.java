package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    Optional<AttributeDefinition> findByNameAndDeletedFalse(String name);
    List<AttributeDefinition> findDistinctByGroups_IdAndDeletedFalse(Long groupId);
    List<AttributeDefinition> findDistinctByGroups_IdInAndDeletedFalse(Iterable<Long> groupIds);
}
