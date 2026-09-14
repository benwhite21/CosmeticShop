package org.example.cosmeticshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.cosmeticshop.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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
    public RedirectView vnpayCallback(@RequestParam Map<String, String> queryParams) {
        boolean isSuccess = paymentService.processCallback(queryParams);
        String orderId = queryParams.getOrDefault("vnp_TxnRef", "");

        if (isSuccess) {
            // Chuyển hướng về giao diện thanh toán thành công
            return new RedirectView("/payment/success?orderId=" + orderId);
        } else {
            // Chuyển hướng về giỏ hàng nếu thất bại kèm cảnh báo
            return new RedirectView("/cart?error=payment_failed");
        }
    }
}