package com.example.orderservice.repository;

import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.OrderEntity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    List<OrderEntity> findOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status);

    List<OrderEntity> findOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate,
            LocalDateTime endDate);
}