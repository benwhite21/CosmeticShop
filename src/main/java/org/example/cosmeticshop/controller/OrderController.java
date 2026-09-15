package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.dto.OrderAdminDto;
import org.example.cosmeticshop.dto.OrderRequest;
import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.OrderStatus;
import org.example.cosmeticshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Đặt hàng mới
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestParam Long userId, @RequestBody OrderRequest request) {
        Order order = orderService.placeOrder(userId, request);
        return ResponseEntity.ok(order);
    }

    // Lấy toàn bộ đơn hàng (Hỗ trợ cả /api/admin/orders và /api/orders để frontend gọi không bị 405)
    @GetMapping({"/admin/orders", "/orders"})
    public ResponseEntity<List<OrderAdminDto>> getAllAdminOrders() {
        List<Order> orders = orderService.getAllOrders();

        List<OrderAdminDto> dtos = orders.stream().map(o -> {
            BigDecimal total = toBigDecimal(o.getTotalAmount());
            BigDecimal finalAmt = o.getFinalAmount() != null ? toBigDecimal(o.getFinalAmount()) : total;
            BigDecimal discount = toBigDecimal(o.getDiscountAmount());

            return new OrderAdminDto(
                    o.getId(),
                    o.getOrderCode(),
                    o.getCustomerName() != null ? o.getCustomerName() : (o.getUser() != null ? o.getUser().getFullName() : "Khách vãng lai"),
                    o.getPhone(),
                    o.getShippingAddress(),
                    o.getPaymentMethod() != null ? o.getPaymentMethod().name() : "COD",
                    total,
                    finalAmt,
                    discount,
                    o.getStatus(),
                    o.getCreatedAt()
            );
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    // Lấy danh sách đơn của một khách hàng
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    // Chi tiết một đơn hàng
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // Cập nhật trạng thái đơn hàng
    @PutMapping({"/admin/orders/{orderId}/status", "/orders/{orderId}/status"})
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam OrderStatus status) {
        Order order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công!", "status", order.getStatus()));
    }

    // Chuyển đổi an toàn Double/BigDecimal sang BigDecimal cho DTO
    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}