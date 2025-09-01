package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndDeletedFalse(Long id);
    List<Product> findAllByDeletedFalse();
}
