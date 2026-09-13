package org.example.cosmeticshop.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long totalCustomers;
    private Long totalProducts;
    private Map<String, Long> ordersByStatus;
    private List<TopSellingProductDTO> topSellingProducts;

    public DashboardStatsDTO() {}

    public DashboardStatsDTO(BigDecimal totalRevenue, Long totalOrders, Long totalCustomers,
                             Long totalProducts, Map<String, Long> ordersByStatus,
                             List<TopSellingProductDTO> topSellingProducts) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalProducts = totalProducts;
        this.ordersByStatus = ordersByStatus;
        this.topSellingProducts = topSellingProducts;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Map<String, Long> getOrdersByStatus() {
        return ordersByStatus;
    }

    public void setOrdersByStatus(Map<String, Long> ordersByStatus) {
        this.ordersByStatus = ordersByStatus;
    }

    public List<TopSellingProductDTO> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopSellingProductDTO> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }
}