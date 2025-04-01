package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JPA 메서드 쿼리
    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceLessThanEqual(Double price);
}