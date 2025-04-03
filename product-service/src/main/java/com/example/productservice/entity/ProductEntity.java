package com.example.productservice.entity;

import com.querydsl.core.annotations.QueryEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@QueryEntity
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @Positive
    private Integer stock;

    private String category;

    @Builder
    public ProductEntity(String name, String description, Double price, Integer stock, String category) {
        // 필수 필드 검증
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("가격은 양수여야 합니다");
        }
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("재고는 0 이상이어야 합니다");
        }

        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // 비즈니스 메서드 - 재고 감소
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다");
        }
        this.stock -= quantity;
    }

    // 비즈니스 메서드 - 재고 증가
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("증가량은 양수여야 합니다");
        }
        this.stock += quantity;
    }
}