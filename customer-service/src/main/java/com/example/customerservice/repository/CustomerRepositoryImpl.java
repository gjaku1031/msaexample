package com.example.customerservice.repository;

import com.example.customerservice.entity.Customer;
import com.example.customerservice.entity.QCustomer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Customer> findByNameContainingWithQuerydsl(String name) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QCustomer customer = QCustomer.customer;

        return queryFactory
                .selectFrom(customer)
                .where(customer.name.contains(name))
                .fetch();
    }
}