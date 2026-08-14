package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.StockUpdateRequest;
import com.inventory.wareflow.entity.Product;
import com.inventory.wareflow.entity.StockRecord;
import com.inventory.wareflow.exception.ResourceNotFoundException;
import com.inventory.wareflow.repository.ProductRepository;
import com.inventory.wareflow.repository.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRecordRepository stockRecordRepository;
    private final ProductRepository productRepository;

    public List<StockRecord> listAll() {
        return stockRecordRepository.findAll();
    }

    /**
     * Updates the stock quantity for a product at a specific warehouse.
     * Creates a new StockRecord if one doesn't already exist for that
     * (product, warehouse) pair.
     */
    public StockRecord updateStock(Long productId, StockUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        StockRecord record = stockRecordRepository
                .findByProductAndWarehouseLocation(product, request.getWarehouseLocation())
                .orElseGet(() -> StockRecord.builder()
                        .product(product)
                        .warehouseLocation(request.getWarehouseLocation())
                        .build());

        record.setQuantity(request.getQuantity());
        record.setLastUpdated(LocalDateTime.now());

        return stockRecordRepository.save(record);
    }
}