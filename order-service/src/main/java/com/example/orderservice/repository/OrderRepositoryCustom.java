package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Order.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}