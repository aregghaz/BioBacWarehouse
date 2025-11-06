package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductBalance;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductBalanceRepository extends JpaRepository<ProductBalance, Long>, JpaSpecificationExecutor<ProductBalance> {
    Optional<ProductBalance> findByWarehouseAndProduct(Warehouse warehouse, Product product);

    @Query("select coalesce(sum(b.balance), 0) from ProductBalance b where b.product.id = :productId")
    Double sumBalanceByProductId(@Param("productId") Long productId);
}
