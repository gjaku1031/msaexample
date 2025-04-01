package com.example.customerservice.repository;

import com.example.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {
    // 기본 CRUD 메서드 및 커스텀 메서드 상속
    // JPA 메서드 쿼리
    Customer findByEmail(String email);
}