package com.example.customerservice.repository;

import com.example.customerservice.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, CustomerRepositoryCustom {
    // 기본 CRUD 메서드 및 커스텀 메서드 상속
    // JPA 메서드 쿼리
    Optional<CustomerEntity> findByEmail(String email);
}