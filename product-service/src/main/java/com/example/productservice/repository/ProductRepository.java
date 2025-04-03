package com.example.productservice.repository;

import com.example.productservice.entity.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>, ProductRepositoryCustom {
    // JPA 메서드 쿼리
    List<ProductEntity> findByCategory(String category);

    List<ProductEntity> findByNameContaining(String name);

    List<ProductEntity> findByPriceLessThanEqual(Double price);
}