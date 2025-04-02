package com.example.orderservice.controller;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.entity.Order.OrderStatus;
import com.example.orderservice.security.Secured;
import com.example.orderservice.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 주문 관련 API 엔드포인트 컨트롤러
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 사용자의 모든 주문 조회
     * 인증된 사용자만 접근 가능 (기본 인증 필터에서 체크)
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        // 현재 인증된 사용자의 주문 목록 조회
        // 실제 구현을 위해 OrderService에 해당 메서드 추가 필요
        List<OrderDto> orders = orderService.getOrdersByCustomerId(null); // 현재 인증된 사용자 ID를 전달하도록 수정 필요
        return ResponseEntity.ok(orders);
    }

    /**
     * 주문 상세 조회
     * 해당 주문의 소유자 또는 ADMIN 권한이 필요
     */
    @GetMapping("/{id}")
    @Secured({ "ROLE_ADMIN", "ORDER:READ" })
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 새 주문 생성
     * 인증된 사용자만 접근 가능 (기본 인증 필터에서 체크)
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody CreateOrderRequest request) {
        OrderDto createdOrder = orderService.createOrder(request);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    /**
     * 주문 취소
     * 해당 주문의 소유자 또는 ADMIN 권한이 필요
     */
    @PutMapping("/{id}/cancel")
    @Secured({ "ROLE_ADMIN", "ORDER:WRITE" })
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long id) {
        OrderDto cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * 모든 주문 조회 (관리자용)
     * ADMIN 권한만 허용
     */
    @GetMapping("/admin/all")
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> allOrders = orderService.getAllOrders();
        return ResponseEntity.ok(allOrders);
    }

    /**
     * 주문 상태 업데이트 (관리자용)
     * ADMIN 권한만 허용
     */
    @PutMapping("/{id}/status")
    @Secured({ "ROLE_ADMIN" })
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByOrderNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderDto>> getOrdersByStatusAndDateRange(
            @RequestParam OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.getOrdersByStatusAndDateRange(status, startDate, endDate));
    }
}