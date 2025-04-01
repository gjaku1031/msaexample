package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Order.OrderStatus;
import com.example.orderservice.entity.QOrder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Order> findOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QOrder order = QOrder.order;

        return queryFactory
                .selectFrom(order)
                .where(
                        order.status.eq(status)
                                .and(order.orderDate.between(startDate, endDate)))
                .orderBy(order.orderDate.desc())
                .fetch();
    }
}