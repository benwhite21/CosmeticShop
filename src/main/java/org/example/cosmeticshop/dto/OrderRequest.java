package org.example.cosmeticshop.dto;

public class OrderRequest {
    private String customerName;
    private String phone;
    private String shippingAddress;
    private String note;
    private String couponCode;

    public OrderRequest() {}

    public OrderRequest(String customerName, String phone, String shippingAddress, String note) {
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.note = note;
    }

    public OrderRequest(String customerName, String phone, String shippingAddress, String note, String couponCode) {
        this.customerName = customerName;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.couponCode = couponCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}