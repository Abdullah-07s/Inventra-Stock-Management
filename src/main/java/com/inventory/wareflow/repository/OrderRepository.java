package com.inventory.wareflow.repository;

import com.inventory.wareflow.entity.Order;
import com.inventory.wareflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);

    List<Order> findByStatus(Order.OrderStatus status);
}