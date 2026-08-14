package com.inventory.wareflow.service;

import com.inventory.wareflow.dto.OrderItemRequest;
import com.inventory.wareflow.dto.OrderRequest;
import com.inventory.wareflow.dto.OrderStatusUpdateRequest;
import com.inventory.wareflow.entity.Order;
import com.inventory.wareflow.entity.OrderItem;
import com.inventory.wareflow.entity.Product;
import com.inventory.wareflow.entity.User;
import com.inventory.wareflow.enums.Role;
import com.inventory.wareflow.exception.ForbiddenException;
import com.inventory.wareflow.exception.ResourceNotFoundException;
import com.inventory.wareflow.repository.OrderRepository;
import com.inventory.wareflow.repository.ProductRepository;
import com.inventory.wareflow.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * USER sees only their own orders; ADMIN/SUPERADMIN see every order
     * in the system, since order oversight is a management capability.
     */
    public List<Order> listOrders() {
        User currentUser = AuthContext.getCurrentUser();

        if (currentUser.getRole() == Role.USER) {
            return orderRepository.findByUser(currentUser);
        }

        return orderRepository.findAll();
    }

    @Transactional
    // @Transactional ensures the Order and all its OrderItems are saved
    // atomically - if anything fails partway through, the whole order rolls back.
    public Order placeOrder(OrderRequest request) {
        User currentUser = AuthContext.getCurrentUser();

        Order order = Order.builder()
                .user(currentUser)
                .status(Order.OrderStatus.PENDING)
                .build();

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPriceAtPurchase(product.getPrice())
                    // Snapshot the current price at time of purchase.
                    .build();
            items.add(item);
        }

        order.setItems(items);
        return orderRepository.save(order);
        // cascade = ALL on Order.items (set in the entity) means saving the
        // Order also saves every OrderItem in one operation.
    }

    public Order updateStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(request.getStatus());
        return orderRepository.save(order);
    }
}