package org.example.cosmeticshop.entity;

public enum OrderStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    SHIPPING("Đang giao"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy");

    private final String description;
    OrderStatus(String description) { this.description = description; }
    public String getDescription() { return description; }


}