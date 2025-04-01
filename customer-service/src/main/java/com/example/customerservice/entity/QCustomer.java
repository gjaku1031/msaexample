package com.example.customerservice.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * 수동으로 생성한 QCustomer 클래스
 */
public class QCustomer extends EntityPathBase<Customer> {

    public static final QCustomer customer = new QCustomer("customer");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final StringPath name = createString("name");
    public final StringPath email = createString("email");
    public final StringPath address = createString("address");
    public final StringPath phoneNumber = createString("phoneNumber");

    public QCustomer(String variable) {
        super(Customer.class, forVariable(variable));
    }

    public QCustomer(Path<? extends Customer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomer(PathMetadata metadata) {
        super(Customer.class, metadata);
    }
}