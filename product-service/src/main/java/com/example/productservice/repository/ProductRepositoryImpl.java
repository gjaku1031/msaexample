package com.example.productservice.repository;

import com.example.productservice.entity.ProductEntity;
import static com.example.productservice.entity.QProductEntity.productEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<ProductEntity> findProductsByCategoryAndMaxPrice(String category, Double maxPrice) {
        return queryFactory
                .selectFrom(productEntity)
                .where(
                        productEntity.category.eq(category)
                                .and(productEntity.price.loe(maxPrice)))
                .orderBy(productEntity.price.asc())
                .fetch();
    }
}