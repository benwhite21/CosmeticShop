package org.example.cosmeticshop.repository;

import org.example.cosmeticshop.dto.TopSellingProductDTO;
import org.example.cosmeticshop.entity.Order;
import org.example.cosmeticshop.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status != :excludeStatus")
    BigDecimal sumTotalRevenueExcludingStatus(@Param("excludeStatus") OrderStatus excludeStatus);

    long countByStatus(OrderStatus status);

    @Query("SELECT new org.example.cosmeticshop.dto.TopSellingProductDTO(oi.product.id, oi.productName, SUM(oi.quantity)) " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.status != :excludeStatus " +
            "GROUP BY oi.product.id, oi.productName " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingProductDTO> findTopSellingProducts(@Param("excludeStatus") OrderStatus excludeStatus, Pageable pageable);
}