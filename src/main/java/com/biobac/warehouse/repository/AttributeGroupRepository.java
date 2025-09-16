package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttributeGroupRepository extends JpaRepository<AttributeGroup, Long>, JpaSpecificationExecutor<AttributeGroup> {
}
