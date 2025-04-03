package com.example.productservice.controller;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.security.RequirePermission;
import com.example.productservice.service.ProductService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 상품 관련 API 엔드포인트 컨트롤러
 */
@RestController
@RequestMapping(path = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 모든 상품 목록 조회
     * 모든 사용자가 접근 가능
     */
    @GetMapping
    public ResponseEntity<List<ProductEntity>> getAllProducts() {
        List<ProductEntity> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 ID로 상품 조회
     * 모든 사용자가 접근 가능
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductEntity> getProductById(@PathVariable Long id) {
        ProductEntity product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductEntity>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping(path = "/search", params = "name")
    public ResponseEntity<List<ProductEntity>> searchProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    @GetMapping(path = "/price", params = "maxPrice")
    public ResponseEntity<List<ProductEntity>> getProductsByMaxPrice(@RequestParam Double maxPrice) {
        return ResponseEntity.ok(productService.getProductsByMaxPrice(maxPrice));
    }

    @GetMapping(path = "/filter", params = { "category", "maxPrice" })
    public ResponseEntity<List<ProductEntity>> getProductsByCategoryAndMaxPrice(
            @RequestParam String category,
            @RequestParam Double maxPrice) {
        return ResponseEntity.ok(productService.getProductsByCategoryAndMaxPrice(category, maxPrice));
    }

    /**
     * 새 상품 생성
     * ADMIN 또는 PRODUCT:WRITE 권한이 필요
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<ProductEntity> createProduct(@RequestBody ProductEntity product) {
        ProductEntity createdProduct = productService.createProduct(product);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdProduct.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(createdProduct);
    }

    /**
     * 상품 정보 업데이트
     * ADMIN 또는 PRODUCT:WRITE 권한이 필요
     */
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<ProductEntity> updateProduct(@PathVariable Long id, @RequestBody ProductEntity product) {
        ProductEntity updatedProduct = productService.updateProduct(id, product);
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
    @PatchMapping(path = "/{id}/stock", params = "quantity")
    @RequirePermission({ "ROLE_ADMIN", "PRODUCT:WRITE" })
    public ResponseEntity<ProductEntity> updateStock(@PathVariable Long id, @RequestParam int quantity) {
        ProductEntity product = productService.updateStock(id, quantity);
        return ResponseEntity.ok(product);
    }
}