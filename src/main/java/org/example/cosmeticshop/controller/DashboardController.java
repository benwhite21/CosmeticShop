package org.example.cosmeticshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cosmeticshop.dto.DashboardStatsDTO;
import org.example.cosmeticshop.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard Controller", description = "API thống kê báo cáo số liệu kinh doanh dành cho Admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Lấy tổng quan số liệu thống kê: Doanh thu, Đơn hàng, Tồn kho, Khách hàng, Top bán chạy")
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}