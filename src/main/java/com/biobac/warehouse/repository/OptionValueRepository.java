package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.AttributeDefinition;
import com.biobac.warehouse.entity.OptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface OptionValueRepository extends JpaRepository<OptionValue, Long>, JpaSpecificationExecutor<OptionValue> {
    List<OptionValue> findAllByAttributeDefinition(AttributeDefinition def);
}
