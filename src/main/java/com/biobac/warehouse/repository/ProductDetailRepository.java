package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long>, JpaSpecificationExecutor<ProductDetail> {
    List<ProductDetail> findByProductBalanceIdOrderByExpirationDateAsc(Long productBalanceId);
}
