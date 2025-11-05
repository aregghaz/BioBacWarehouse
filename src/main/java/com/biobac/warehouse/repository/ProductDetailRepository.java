package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.IngredientDetail;
import com.biobac.warehouse.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long>, JpaSpecificationExecutor<ProductDetail> {
    List<ProductDetail> findByProductBalanceIdOrderByExpirationDateAsc(Long productBalanceId);

    Optional<ProductDetail> findByProductBalanceIdAndExpirationDate(Long id, LocalDateTime expirationDate);
}
