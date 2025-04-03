package com.example.customerservice.repository;

import static com.example.customerservice.entity.QCustomerEntity.customerEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.example.customerservice.entity.CustomerEntity;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CustomerRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<CustomerEntity> searchCustomers(String nameOrEmail) {
        if (!StringUtils.hasText(nameOrEmail)) {
            return List.of();
        }

        return queryFactory
                .selectFrom(customerEntity)
                .where(nameOrEmailContains(nameOrEmail))
                .orderBy(customerEntity.name.asc())
                .fetch();
    }

    private BooleanExpression nameOrEmailContains(String keyword) {
        return customerEntity.name.containsIgnoreCase(keyword)
                .or(customerEntity.email.containsIgnoreCase(keyword));
    }
}