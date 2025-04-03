package com.example.orderservice.repository;

import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.OrderEntity.OrderStatus;
import static com.example.orderservice.entity.QOrderEntity.orderEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

        private final JPAQueryFactory queryFactory;

        public OrderRepositoryImpl(EntityManager entityManager) {
                this.queryFactory = new JPAQueryFactory(entityManager);
        }

        @Override
        public List<OrderEntity> findOrdersByCustomerIdAndStatus(Long customerId, OrderStatus status) {
                return queryFactory
                                .selectFrom(orderEntity)
                                .where(
                                                orderEntity.customerId.eq(customerId)
                                                                .and(orderEntity.status.eq(status)))
                                .orderBy(orderEntity.orderDate.desc())
                                .fetch();
        }

        @Override
        public List<OrderEntity> findOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate,
                        LocalDateTime endDate) {
                return queryFactory
                                .selectFrom(orderEntity)
                                .where(
                                                orderEntity.status.eq(status)
                                                                .and(orderEntity.orderDate.between(startDate, endDate)))
                                .orderBy(orderEntity.orderDate.desc())
                                .fetch();
        }
}