package com.inventory.wareflow.repository;

import com.inventory.wareflow.entity.Order;
import com.inventory.wareflow.entity.OrderItem;
import com.inventory.wareflow.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

    void deleteByProduct(Product product);
}