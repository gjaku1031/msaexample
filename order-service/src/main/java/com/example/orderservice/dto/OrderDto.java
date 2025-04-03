package com.example.orderservice.dto;

import com.example.orderservice.entity.OrderEntity.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long customerId;
    private String customerName;
    private String orderNumber;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private List<OrderItemDto> items;
}