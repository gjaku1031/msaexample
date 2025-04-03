package com.example.orderservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Builder
    public OrderItemEntity(Long productId, String productName, Integer quantity, Double unitPrice) {
        // 필수 필드 검증
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다");
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        if (unitPrice == null || unitPrice <= 0) {
            throw new IllegalArgumentException("단가는 양수여야 합니다");
        }

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = calculateTotalPrice();
    }

    // 비즈니스 로직
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다");
        }
        this.quantity = newQuantity;
        this.totalPrice = calculateTotalPrice();
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    private Double calculateTotalPrice() {
        return this.quantity * this.unitPrice;
    }
}