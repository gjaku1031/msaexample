package com.example.productservice.entity;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * 수동으로 생성한 QProduct 클래스
 */
public class QProduct extends EntityPathBase<Product> {

    public static final QProduct product = new QProduct("product");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final StringPath name = createString("name");
    public final StringPath description = createString("description");
    public final NumberPath<Double> price = createNumber("price", Double.class);
    public final NumberPath<Integer> stock = createNumber("stock", Integer.class);
    public final StringPath category = createString("category");

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }
}