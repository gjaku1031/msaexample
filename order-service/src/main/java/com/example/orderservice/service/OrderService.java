package com.example.orderservice.service;

import com.example.orderservice.client.CustomerClient;
import com.example.orderservice.client.ProductClient;
import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.dto.CustomerDto;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.dto.ProductDto;
import com.example.orderservice.entity.OrderEntity;
import com.example.orderservice.entity.OrderEntity.OrderStatus;
import com.example.orderservice.entity.OrderItemEntity;
import com.example.orderservice.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final CustomerClient customerClient;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        CustomerDto customer = customerClient.getCustomer(request.getCustomerId());

        // 주문 생성
        OrderEntity order = OrderEntity.builder()
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

            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
            totalAmount += orderItem.getUnitPrice() * orderItem.getQuantity();
        }

        // totalAmount는 addOrderItem 메서드에서 자동 계산됨
        OrderEntity savedOrder = orderRepository.save(order);

        return mapToOrderDto(savedOrder, customer.getName());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
        return mapToOrderDto(order, customer.getName());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByOrderNumber(String orderNumber) {
        List<OrderEntity> orders = orderRepository.findByOrderNumber(orderNumber);
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("Order not found with orderNumber: " + orderNumber);
        }

        OrderEntity order = orders.get(0);
        CustomerDto customer = customerClient.getCustomer(order.getCustomerId());
        return mapToOrderDto(order, customer.getName());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomerId(Long customerId) {
        CustomerDto customer = customerClient.getCustomer(customerId);
        List<OrderEntity> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream()
                .map(order -> mapToOrderDto(order, customer.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        List<OrderEntity> orders = orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus() == status)
                .collect(Collectors.toList());

        Map<Long, String> customerNames = orders.stream()
                .map(OrderEntity::getCustomerId)
                .distinct()
                .collect(Collectors.toMap(
                        (Long id) -> id,
                        (Long id) -> {
                            try {
                                return customerClient.getCustomer(id).getName();
                            } catch (Exception e) {
                                return "Unknown Customer";
                            }
                        }));

        return orders.stream()
                .map(order -> mapToOrderDto(order, customerNames.get(order.getCustomerId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<OrderEntity> orders = orderRepository.findOrdersByStatusAndDateRange(status, startDate, endDate);

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
        List<OrderEntity> orders = orderRepository.findAll();

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
        OrderEntity order = orderRepository.findById(id)
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
        order.updateStatus(OrderStatus.CANCELLED);
        OrderEntity updatedOrder = orderRepository.save(order);

        // 재고 복원 로직 (옵션)
        for (OrderItemEntity item : order.getOrderItems()) {
            productClient.updateStock(item.getProductId(), -item.getQuantity()); // 음수로 전달하여 재고 증가
        }

        CustomerDto customer = customerClient.getCustomer(updatedOrder.getCustomerId());
        return mapToOrderDto(updatedOrder, customer.getName());
    }

    @Transactional
    public OrderDto updateOrderStatus(Long id, OrderStatus status) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.updateStatus(status);
        OrderEntity updatedOrder = orderRepository.save(order);

        CustomerDto customer = customerClient.getCustomer(updatedOrder.getCustomerId());
        return mapToOrderDto(updatedOrder, customer.getName());
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private OrderDto mapToOrderDto(OrderEntity order, String customerName) {
        List<OrderItemDto> orderItemDtos = order.getOrderItems().stream()
                .map(this::mapToOrderItemDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .customerName(customerName)
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .items(orderItemDtos)
                .build();
    }

    private OrderItemDto mapToOrderItemDto(OrderItemEntity orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}