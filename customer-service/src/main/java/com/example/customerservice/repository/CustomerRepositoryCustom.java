package com.example.customerservice.repository;

import com.example.customerservice.entity.Customer;
import java.util.List;

public interface CustomerRepositoryCustom {
    List<Customer> findByNameContainingWithQuerydsl(String name);
}