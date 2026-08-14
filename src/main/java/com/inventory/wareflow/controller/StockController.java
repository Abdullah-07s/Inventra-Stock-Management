package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.StockUpdateRequest;
import com.inventory.wareflow.entity.StockRecord;
import com.inventory.wareflow.enums.Activity;
import com.inventory.wareflow.security.RequiresActivity;
import com.inventory.wareflow.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<StockRecord> listAll() {
        return stockService.listAll();
    }

    @PutMapping("/{productId}")
    @RequiresActivity(Activity.MANAGE_STOCK)
    public StockRecord updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateRequest request) {
        return stockService.updateStock(productId, request);
    }
}