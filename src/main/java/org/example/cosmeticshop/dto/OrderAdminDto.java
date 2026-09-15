package org.example.cosmeticshop.dto;

import org.example.cosmeticshop.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderAdminDto {
    private Long id;
    private String orderCode;
    private String customerName;
    private String phone;
    private String shippingAddress;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private BigDecimal discountAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderAdminDto() {}

    public OrderAdminDto(Long id, String orderCode, String customerName, String phone,
                         String shippingAddress, String paymentMethod, BigDecimal totalAmount,
                         BigDecimal finalAmount, BigDecimal discountAmount, OrderStatus status,
                         LocalDateTime createdAt) {
        this.id = id;
        this.orderCode = orderCode;
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.discountAmount = discountAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}