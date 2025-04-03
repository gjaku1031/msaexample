package com.example.orderservice.repository;

import com.example.orderservice.entity.OrderEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, OrderRepositoryCustom {
    List<OrderEntity> findByCustomerId(Long customerId);

    List<OrderEntity> findByOrderNumber(String orderNumber);
}