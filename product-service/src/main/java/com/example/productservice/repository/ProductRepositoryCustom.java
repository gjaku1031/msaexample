package com.example.productservice.repository;

import com.example.productservice.entity.ProductEntity;
import java.util.List;

public interface ProductRepositoryCustom {
    List<ProductEntity> findProductsByCategoryAndMaxPrice(String category, Double maxPrice);
}