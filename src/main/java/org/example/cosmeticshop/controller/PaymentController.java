package org.example.cosmeticshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.cosmeticshop.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payment Controller", description = "Tích hợp cổng thanh toán trực tuyến VNPAY")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Tạo liên kết thanh toán VNPAY cho đơn hàng")
    @GetMapping("/create-vnpay/{orderId}")
    public ResponseEntity<?> createVnPayPayment(@PathVariable Long orderId, HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createVnPayPayment(orderId, request);
            return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "VNPAY callback xử lý kết quả thanh toán")
    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> vnpayCallback(@RequestParam Map<String, String> queryParams) {
        boolean isSuccess = paymentService.processCallback(queryParams);
        if (isSuccess) {
            return ResponseEntity.ok("Thanh toán thành công! Đơn hàng đã được xác nhận.");
        } else {
            return ResponseEntity.badRequest().body("Thanh toán thất bại hoặc bị hủy bởi khách hàng.");
        }
    }
}