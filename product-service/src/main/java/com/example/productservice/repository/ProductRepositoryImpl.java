package com.example.productservice.repository;

import com.example.productservice.entity.Product;
import com.example.productservice.entity.QProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findProductsByCategoryAndMaxPrice(String category, Double maxPrice) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QProduct product = QProduct.product;

        return queryFactory
                .selectFrom(product)
                .where(
                        product.category.eq(category)
                                .and(product.price.loe(maxPrice)))
                .orderBy(product.price.asc())
                .fetch();
    }
}