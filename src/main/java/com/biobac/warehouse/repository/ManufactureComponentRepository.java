package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ManufactureComponent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufactureComponentRepository extends JpaRepository<ManufactureComponent, Long> {
}
