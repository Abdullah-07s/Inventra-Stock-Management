package com.inventory.wareflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/stock/{productId}. Updates (or creates, if
 * absent) the stock record for a product at a specific warehouse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    @NotBlank(message = "Warehouse location is required")
    private String warehouseLocation;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
}