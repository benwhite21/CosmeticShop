package org.example.cosmeticshop.service;

import org.example.cosmeticshop.dto.DashboardStatsDTO;
import org.example.cosmeticshop.dto.TopSellingProductDTO;
import org.example.cosmeticshop.entity.OrderStatus;
import org.example.cosmeticshop.repository.OrderRepository;
import org.example.cosmeticshop.repository.ProductRepository;
import org.example.cosmeticshop.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public DashboardService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalRevenueExcludingStatus(OrderStatus.CANCELLED);
        long totalOrders = orderRepository.count();
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), orderRepository.countByStatus(status));
        }

        List<TopSellingProductDTO> topSelling = orderRepository.findTopSellingProducts(
                OrderStatus.CANCELLED, PageRequest.of(0, 5)
        );

        return new DashboardStatsDTO(totalRevenue, totalOrders, totalCustomers, totalProducts, ordersByStatus, topSelling);
    }
}