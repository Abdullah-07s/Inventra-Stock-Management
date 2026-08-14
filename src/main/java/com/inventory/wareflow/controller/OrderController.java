package com.inventory.wareflow.controller;

import com.inventory.wareflow.dto.OrderRequest;
import com.inventory.wareflow.dto.OrderResponse;
import com.inventory.wareflow.dto.OrderStatusUpdateRequest;
import com.inventory.wareflow.entity.Order;
import com.inventory.wareflow.enums.Activity;
import com.inventory.wareflow.security.RequiresActivity;
import com.inventory.wareflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> listOrders() {
        return orderService.listOrders().stream()
                .map(OrderResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        Order created = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(created));
    }

    @PutMapping("/{id}/status")
    @RequiresActivity(Activity.MANAGE_ORDERS)
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        Order updated = orderService.updateStatus(id, request);
        return OrderResponse.from(updated);
    }
}