package com.example.lipsticks.home.controller;

import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final OrderService orderService;

    @GetMapping("/modules")
    public ApiResponse<List<Map<String, String>>> modules() {
        List<Map<String, String>> modules = List.of(
                Map.of("name", "Lipstick Mall", "path", "/user/mall"),
                Map.of("name", "Product Recommend", "path", "/user/recommend"),
                Map.of("name", "Color Visualization", "path", "/user/visualization")
        );
        return ApiResponse.ok(modules);
    }

    /**
     * 当前用户已购买的商品列表（已支付订单中的商品）
     * 用户主页展示已购商品
     */
    @GetMapping("/purchased-products")
    public ApiResponse<List<LipstickProduct>> purchasedProducts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.ok(List.of());
        }
        return ApiResponse.ok(orderService.getPurchasedProducts(authentication.getName()));
    }
}
