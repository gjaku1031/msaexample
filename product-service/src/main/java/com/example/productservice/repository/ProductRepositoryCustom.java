package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import java.util.List;

public interface ProductRepositoryCustom {
    List<Product> findProductsByCategoryAndMaxPrice(String category, Double maxPrice);
}