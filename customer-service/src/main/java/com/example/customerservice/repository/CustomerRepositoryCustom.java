package com.example.customerservice.repository;

import com.example.customerservice.entity.CustomerEntity;
import java.util.List;

public interface CustomerRepositoryCustom {
    List<CustomerEntity> searchCustomers(String nameOrEmail);
}