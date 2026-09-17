package com.example.lipsticks.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.order.dto.CreateOrderRequest;
import com.example.lipsticks.order.dto.OrderResponse;
import com.example.lipsticks.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(Authentication authentication,
                                                   @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(authentication.getName(), request);
        return ApiResponse.ok(response);
    }

    /**
     * 我的订单列表（分页）
     */
    @GetMapping
    public ApiResponse<Page<OrderResponse>> listMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.listUserOrders(authentication.getName(), page, size, status));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> getOrderDetail(Authentication authentication,
                                                      @PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getOrderDetail(authentication.getName(), orderNo));
    }
}
