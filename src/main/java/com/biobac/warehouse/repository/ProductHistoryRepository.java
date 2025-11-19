package com.biobac.warehouse.repository;

import com.biobac.warehouse.entity.Product;
import com.biobac.warehouse.entity.ProductHistory;
import com.biobac.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long>, JpaSpecificationExecutor<ProductHistory> {
    @Query(value = "SELECT * FROM product_history WHERE product_id = :productId ORDER BY timestamp ASC, id ASC LIMIT 1", nativeQuery = true)
    ProductHistory findEarliestByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT * FROM product_history WHERE product_id = :productId ORDER BY timestamp DESC, id DESC LIMIT 1", nativeQuery = true)
    ProductHistory findLatestByProductId(@Param("productId") Long productId);

    // Last record up to a given end timestamp (inclusive)
    @Query(value = "SELECT * FROM product_history WHERE product_id = :productId AND timestamp <= :end ORDER BY timestamp DESC, id DESC LIMIT 1", nativeQuery = true)
    ProductHistory findLastInRange(@Param("productId") Long productId, @Param("end") LocalDateTime end);

    // Last record up to a given end timestamp (inclusive) filtered by warehouse
    @Query(value = "SELECT * FROM product_history WHERE product_id = :productId AND warehouse_id = :warehouseId AND timestamp <= :end ORDER BY timestamp DESC, id DESC LIMIT 1", nativeQuery = true)
    ProductHistory findLastInRangeWithWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId, @Param("end") LocalDateTime end);

    @Query(value = "SELECT * FROM product_history WHERE product_id = :productId AND timestamp < :start ORDER BY timestamp DESC, id DESC LIMIT 1", nativeQuery = true)
    ProductHistory findFirstBeforeRange(@Param("productId") Long productId, @Param("start") LocalDateTime start);

    @Query(value = """
            select sum(ph.quantity_change) as sum
            from product_history ph
                     join product p on p.id = ph.product_id
            where ph.increase = true
              and ph.timestamp between :from and :to
              and p.id = :product_id
            """, nativeQuery = true)
    Double sumIncreasedCount(@Param("product_id") Long productId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    @Query(value = """
            select sum(ph.quantity_change) as sum
            from product_history ph
                     join product p on p.id = ph.product_id
            where ph.increase = false
              and ph.timestamp between :from and :to
              and p.id = :product_id
            """, nativeQuery = true)
    Double sumDecreasedCount(@Param("product_id") Long productId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);

    Optional<ProductHistory> findFirstByWarehouseAndProductOrderByTimestampDescIdDesc(Warehouse warehouse, Product product);
}