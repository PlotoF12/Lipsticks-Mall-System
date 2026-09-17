package com.example.lipsticks.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.order.dto.OrderResponse;
import com.example.lipsticks.order.dto.OrderStatisticsResponse;
import com.example.lipsticks.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 管理员查看所有订单（分页，支持按状态和用户名筛选）
     */
    @GetMapping
    public ApiResponse<Page<OrderResponse>> listAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username) {
        return ApiResponse.ok(orderService.listAllOrders(page, size, status, username));
    }

    /**
     * 管理员查看订单详情
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> getOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getOrderDetailForAdmin(orderNo));
    }

    /**
     * 管理员更新订单状态
     * PUT /api/admin/orders/{orderNo}/status
     * Body: { "status": "COMPLETED" | "CANCELLED" }
     */
    @PutMapping("/{orderNo}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderNo,
            @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        return ApiResponse.ok(orderService.updateOrderStatus(orderNo, newStatus));
    }

    /**
     * 管理员获取订单统计数据
     */
    @GetMapping("/statistics")
    public ApiResponse<OrderStatisticsResponse> getStatistics() {
        return ApiResponse.ok(orderService.getStatistics());
    }
}
