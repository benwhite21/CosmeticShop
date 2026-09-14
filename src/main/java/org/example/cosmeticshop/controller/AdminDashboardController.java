package org.example.cosmeticshop.controller;

import org.example.cosmeticshop.dto.AdminDashboardStats;
import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.OrderStatus;
import org.example.cosmeticshop.entity.Product;
import org.example.cosmeticshop.repository.OrderRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminDashboardController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminDashboardController(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardStats> getStats() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Tính tổng doanh thu từ các đơn thành công (DELIVERED)
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (Order o : allOrders) {
            if (o.getStatus() == OrderStatus.DELIVERED) {
                Double rawAmount = 0.0;
                if (o.getFinalAmount() != null) {
                    rawAmount = o.getFinalAmount().doubleValue();
                } else if (o.getTotalAmount() != null) {
                    rawAmount = o.getTotalAmount().doubleValue();
                }
                totalRevenue = totalRevenue.add(BigDecimal.valueOf(rawAmount));
            }
        }

        // 2. Đếm số đơn hoàn thành & số đơn chờ duyệt
        long completedCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long pendingCount = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();

        // 3. Tổng số lượng sản phẩm
        long totalProducts = productRepository.count();

        // 4. Sản phẩm cảnh báo sắp hết hàng (tồn kho <= 5)
        List<Product> lowStockProducts = productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .toList();

        // 5. 5 đơn hàng mới nhất
        List<Order> recentOrders = orderRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"))
        ).getContent();

        // 6. Tính doanh thu 7 ngày gần nhất cho biểu đồ Chart.js
        List<String> labels = new ArrayList<>();
        List<Double> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);
            labels.add(targetDate.format(formatter));

            double daySum = 0.0;
            for (Order o : allOrders) {
                if (o.getStatus() == OrderStatus.DELIVERED && o.getCreatedAt() != null) {
                    if (o.getCreatedAt().toLocalDate().isEqual(targetDate)) {
                        Double val = o.getFinalAmount() != null ? o.getFinalAmount().doubleValue() :
                                (o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0);
                        daySum += val;
                    }
                }
            }
            chartData.add(daySum);
        }

        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalRevenue(totalRevenue);
        stats.setCompletedOrdersCount(completedCount);
        stats.setPendingOrdersCount(pendingCount);
        stats.setTotalProductsCount(totalProducts);
        stats.setLowStockProducts(lowStockProducts);
        stats.setRecentOrders(recentOrders);
        stats.setDailyRevenueLabels(labels);
        stats.setDailyRevenueData(chartData);

        return ResponseEntity.ok(stats);
    }
}