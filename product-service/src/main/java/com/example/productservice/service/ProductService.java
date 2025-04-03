package com.example.productservice.service;

import com.example.productservice.entity.ProductEntity;
import com.example.productservice.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductEntity> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ProductEntity getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> searchProductsByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getProductsByMaxPrice(Double maxPrice) {
        return productRepository.findByPriceLessThanEqual(maxPrice);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> getProductsByCategoryAndMaxPrice(String category, Double maxPrice) {
        return productRepository.findProductsByCategoryAndMaxPrice(category, maxPrice);
    }

    @Transactional
    public ProductEntity createProduct(ProductEntity product) {
        return productRepository.save(product);
    }

    @Transactional
    public ProductEntity updateProduct(Long id, ProductEntity product) {
        // 기존 상품 조회
        ProductEntity existingProduct = getProductById(id);

        // 새로운 불변 객체 생성 (Builder 패턴 사용)
        ProductEntity updatedProduct = ProductEntity.builder()
                .name(product.getName() != null ? product.getName() : existingProduct.getName())
                .description(
                        product.getDescription() != null ? product.getDescription() : existingProduct.getDescription())
                .price(product.getPrice() != null ? product.getPrice() : existingProduct.getPrice())
                .stock(product.getStock() != null ? product.getStock() : existingProduct.getStock())
                .category(product.getCategory() != null ? product.getCategory() : existingProduct.getCategory())
                .build();

        // ID 값 설정을 위한 리플렉션 사용 대신 새 객체로 저장
        productRepository.delete(existingProduct);
        return productRepository.save(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = getProductById(id);
        productRepository.delete(product);
    }

    @Transactional
    public ProductEntity updateStock(Long id, Integer quantity) {
        ProductEntity existingProduct = getProductById(id);

        // 재고 검증
        if (existingProduct.getStock() < quantity) {
            throw new RuntimeException("Not enough stock for product: " + id);
        }

        // 새 객체 생성
        ProductEntity updatedProduct = ProductEntity.builder()
                .name(existingProduct.getName())
                .description(existingProduct.getDescription())
                .price(existingProduct.getPrice())
                .stock(existingProduct.getStock() - quantity)
                .category(existingProduct.getCategory())
                .build();

        // 상품 업데이트
        productRepository.delete(existingProduct);
        return productRepository.save(updatedProduct);
    }
}