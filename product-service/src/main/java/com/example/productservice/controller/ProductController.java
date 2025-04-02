package com.example.productservice.controller;

import com.example.productservice.entity.Product;
import com.example.productservice.security.RequirePermission;
import com.example.productservice.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 관련 API 엔드포인트 컨트롤러
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 모든 상품 목록 조회
     * 모든 사용자가 접근 가능
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 ID로 상품 조회
     * 모든 사용자가 접근 가능
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    @GetMapping("/price")
    public ResponseEntity<List<Product>> getProductsByMaxPrice(@RequestParam Double maxPrice) {
        return ResponseEntity.ok(productService.getProductsByMaxPrice(maxPrice));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Product>> getProductsByCategoryAndMaxPrice(
            @RequestParam String category,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(productService.getProductsByCategoryAndMaxPrice(category, maxPrice));
    }

    /**
     * 새 상품 생성
     * ADMIN 또는 PRODUCT:WRITE 권한이 필요
     */
    @PostMapping
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * 상품 정보 업데이트
     * ADMIN 또는 PRODUCT:WRITE 권한이 필요
     */
    @PutMapping("/{id}")
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 상품 삭제
     * ADMIN 권한만 허용
     */
    @DeleteMapping("/{id}")
    @RequirePermission({ "ROLE_ADMIN" })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 상품 재고 업데이트
     * ADMIN 또는 PRODUCT:WRITE 권한이 필요
     */
    @PatchMapping("/{id}/stock")
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        Product product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }
}