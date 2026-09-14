package org.example.cosmeticshop.dto;

import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.Product;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardStats {
    private BigDecimal totalRevenue;
    private long completedOrdersCount;
    private long pendingOrdersCount;
    private long totalProductsCount;
    private List<Product> lowStockProducts;
    private List<Order> recentOrders;
    private List<String> dailyRevenueLabels;
    private List<Double> dailyRevenueData;

    public AdminDashboardStats() {}

    public AdminDashboardStats(BigDecimal totalRevenue, long completedOrdersCount,
                               long pendingOrdersCount, long totalProductsCount,
                               List<Product> lowStockProducts, List<Order> recentOrders,
                               List<String> dailyRevenueLabels, List<Double> dailyRevenueData) {
        this.totalRevenue = totalRevenue;
        this.completedOrdersCount = completedOrdersCount;
        this.pendingOrdersCount = pendingOrdersCount;
        this.totalProductsCount = totalProductsCount;
        this.lowStockProducts = lowStockProducts;
        this.recentOrders = recentOrders;
        this.dailyRevenueLabels = dailyRevenueLabels;
        this.dailyRevenueData = dailyRevenueData;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getCompletedOrdersCount() {
        return completedOrdersCount;
    }

    public void setCompletedOrdersCount(long completedOrdersCount) {
        this.completedOrdersCount = completedOrdersCount;
    }

    public long getPendingOrdersCount() {
        return pendingOrdersCount;
    }

    public void setPendingOrdersCount(long pendingOrdersCount) {
        this.pendingOrdersCount = pendingOrdersCount;
    }

    public long getTotalProductsCount() {
        return totalProductsCount;
    }

    public void setTotalProductsCount(long totalProductsCount) {
        this.totalProductsCount = totalProductsCount;
    }

    public List<Product> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<Product> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<String> getDailyRevenueLabels() {
        return dailyRevenueLabels;
    }

    public void setDailyRevenueLabels(List<String> dailyRevenueLabels) {
        this.dailyRevenueLabels = dailyRevenueLabels;
    }

    public List<Double> getDailyRevenueData() {
        return dailyRevenueData;
    }

    public void setDailyRevenueData(List<Double> dailyRevenueData) {
        this.dailyRevenueData = dailyRevenueData;
    }
}