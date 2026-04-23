package com.example.lipsticks.home.controller;

import com.example.lipsticks.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @GetMapping("/modules")
    public ApiResponse<List<Map<String, String>>> modules() {
        List<Map<String, String>> modules = List.of(
                Map.of("name", "Lipstick Mall", "path", "/user/mall"),
                Map.of("name", "Product Recommend", "path", "/user/recommend"),
                Map.of("name", "Color Visualization", "path", "/user/visualization")
        );
        return ApiResponse.ok(modules);
    }
}
