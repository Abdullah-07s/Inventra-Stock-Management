package com.inventory.wareflow.repository;

import com.inventory.wareflow.entity.Product;
import com.inventory.wareflow.entity.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {
    List<StockRecord> findByProduct(Product product);

    Optional<StockRecord> findByProductAndWarehouseLocation(Product product, String warehouseLocation);

    void deleteByProduct(Product product);
}