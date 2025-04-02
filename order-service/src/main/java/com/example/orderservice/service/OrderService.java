package com.example.orderservice.service;

import com.example.orderservice.client.CustomerClient;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.CustomerDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.Order.OrderStatus;
import com.example.orderservice.entity.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OrderRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderRepositoryImpl orderRepositoryImpl;
    private final ProductClient productClient;
    private final CustomerClient customerClient;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        CustomerDto customer = customerClient.getCustomer(request.getCustomerId());

        // 주문 생성
        Order order = Order.builder()
                .customerId(customer.getId())
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.CREATED)
                .orderDate(LocalDateTime.now())
                .totalAmount(0.0)
                .build();

        // 주문 상품 추가
        double totalAmount = 0.0;
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            ProductDto product = productClient.getProduct(itemRequest.getProductId());

            // 재고 업데이트
            productClient.updateStock(product.getId(), itemRequest.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .totalPrice(product.getPrice() * itemRequest.getQuantity())
                    .build();

            order.addOrderItem(orderItem);
            totalAmount += orderItem.getTotalPrice();
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToOrderDto(savedOrder, customer.getName());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
        return mapToOrderDto(order, customer.getName());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new RuntimeException("Order not found with orderNumber: " + orderNumber);
        }

        CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
        return mapToOrderDto(order, customer.getName());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        CustomerDto customer = customerClient.getCustomer(customerId);
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        return orders.stream()
                .map(order -> mapToOrderDto(order, customer.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Order> orders = orderRepositoryImpl.findOrdersByStatusAndDateRange(status, startDate, endDate);

        return orders.stream()
                .map(order -> {
                    CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
                    return mapToOrderDto(order, customer.getName());
                })
                .collect(Collectors.toList());
    }

    /**
     * 모든 주문 목록을 조회합니다.
     * 관리자 권한이 필요한 작업입니다.
     * 
     * @return 모든 주문 목록
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream()
                .map(order -> {
                    CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
                    return mapToOrderDto(order, customer.getName());
                })
                .collect(Collectors.toList());
    }

    /**
     * 주문을 취소합니다.
     * 관리자 또는 주문 작성 권한이 있는 사용자만 주문을 취소할 수 있습니다.
     *
     * @param id 취소할 주문 ID
     * @return 취소된 주문 정보
     */
    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        // 이미 취소된 주문인지 확인
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is already cancelled: " + id);
        }

        // 배송 완료된 주문은 취소 불가능
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel delivered order: " + id);
        }

        // 주문 상태를 취소로 변경
        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        // 재고 복원 로직 (옵션)
        for (OrderItem item : order.getOrderItems()) {
            productClient.updateStock(item.getProductId(), -item.getQuantity()); // 음수로 전달하여 재고 증가
        }

        CustomerDto customer = customerClient.getCustomer(updatedOrder.getCustomerId());
        return mapToOrderDto(updatedOrder, customer.getName());
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        CustomerDto customer = customerClient.getCustomer(updatedOrder.getCustomerId());
        return mapToOrderDto(updatedOrder, customer.getName());
    }

    private String generateOrderNumber() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderDto mapToOrderDto(Order order, String customerName) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .items(orderItemDtos)
                .build();
    }
}