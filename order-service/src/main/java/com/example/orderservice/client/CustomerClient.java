package com.example.orderservice.client;

import com.example.orderservice.config.FeignClientConfig;
import com.example.orderservice.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", configuration = FeignClientConfig.class)
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    CustomerDto getCustomer(@PathVariable("id") Long id);
}