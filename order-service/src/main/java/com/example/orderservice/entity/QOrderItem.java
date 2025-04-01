package com.example.orderservice.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * 수동으로 생성한 QOrderItem 클래스
 */
public class QOrderItem extends EntityPathBase<OrderItem> {

    public static final QOrderItem orderItem = new QOrderItem("orderItem");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final NumberPath<Long> productId = createNumber("productId", Long.class);
    public final StringPath productName = createString("productName");
    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);
    public final NumberPath<Double> price = createNumber("price", Double.class);

    // Order 참조 (양방향 관계)
    public final QOrder order;

    public QOrderItem(String variable) {
        this(forVariable(variable), PathInits.DIRECT2);
    }

    public QOrderItem(Path<? extends OrderItem> path) {
        this(path.getMetadata(), PathInits.DIRECT2);
    }

    public QOrderItem(PathMetadata metadata) {
        this(metadata, PathInits.DIRECT2);
    }

    public QOrderItem(PathMetadata metadata, PathInits inits) {
        super(OrderItem.class, metadata);
        this.order = inits.isInitialized("order") ? new QOrder(forProperty("order")) : null;
    }
}