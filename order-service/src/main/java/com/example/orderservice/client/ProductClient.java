package com.example.orderservice.client;

import com.example.orderservice.config.FeignClientConfig;
import com.example.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service", configuration = FeignClientConfig.class)
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    @PatchMapping("/api/products/{id}/stock")
    ProductDto updateStock(@PathVariable("id") Long id, @RequestParam Integer quantity);
}