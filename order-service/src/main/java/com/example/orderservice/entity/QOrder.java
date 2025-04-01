package com.example.orderservice.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.time.LocalDateTime;

/**
 * 수동으로 생성한 QOrder 클래스
 */
public class QOrder extends EntityPathBase<Order> {

    public static final QOrder order = new QOrder("order1"); // "order"는 SQL 예약어이므로 "order1"로 사용

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final NumberPath<Long> customerId = createNumber("customerId", Long.class);
    public final StringPath orderNumber = createString("orderNumber");
    public final EnumPath<Order.OrderStatus> status = createEnum("status", Order.OrderStatus.class);
    public final DateTimePath<LocalDateTime> orderDate = createDateTime("orderDate", LocalDateTime.class);
    public final NumberPath<Double> totalAmount = createNumber("totalAmount", Double.class);

    // OrderItems 컬렉션 (양방향 관계)
    public final ListPath<OrderItem, QOrderItem> orderItems;

    public QOrder(String variable) {
        this(forVariable(variable), PathInits.DIRECT2);
    }

    public QOrder(Path<? extends Order> path) {
        this(path.getMetadata(), PathInits.DIRECT2);
    }

    public QOrder(PathMetadata metadata) {
        this(metadata, PathInits.DIRECT2);
    }

    public QOrder(PathMetadata metadata, PathInits inits) {
        super(Order.class, metadata);
        this.orderItems = inits.isInitialized("orderItems")
                ? this.<OrderItem, QOrderItem>createList("orderItems", OrderItem.class, QOrderItem.class,
                        PathInits.DIRECT2)
                : null;
    }
}