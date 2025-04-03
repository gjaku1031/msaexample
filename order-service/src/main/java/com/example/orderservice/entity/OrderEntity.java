package com.example.orderservice.entity;

import com.querydsl.core.annotations.QueryEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@QueryEntity
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    private Double totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    public enum OrderStatus {
        CREATED, PROCESSING, COMPLETED, CANCELLED, DELIVERED
    }

    @Builder
    public OrderEntity(Long customerId, String orderNumber, OrderStatus status, LocalDateTime orderDate,
            Double totalAmount) {
        // 필수 필드 검증
        if (customerId == null) {
            throw new IllegalArgumentException("고객 ID는 필수입니다");
        }
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("주문번호는 필수입니다");
        }

        this.customerId = customerId;
        this.orderNumber = orderNumber;
        this.status = status != null ? status : OrderStatus.CREATED;
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now();
        this.totalAmount = totalAmount != null ? totalAmount : 0.0;
        this.orderItems = new ArrayList<>();
    }

    public void addOrderItem(OrderItemEntity orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
        recalculateTotalAmount();
    }

    public void removeOrderItem(OrderItemEntity orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
        recalculateTotalAmount();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    private void recalculateTotalAmount() {
        this.totalAmount = orderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }
}