package com.example.lipsticks.mall.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.service.MallService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mall")
@RequiredArgsConstructor
public class MallController {

    private final MallService mallService;

    @GetMapping("/products/page")
    public ApiResponse<Page<LipstickProduct>> pageProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.ok(mallService.pageProducts(page, size, keyword, brand, category));
    }

    @GetMapping("/products")
    public ApiResponse<List<LipstickProduct>> products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category
    ) {
        if (keyword != null || brand != null || category != null) {
            return ApiResponse.ok(mallService.searchProducts(keyword, brand, category));
        }
        return ApiResponse.ok(mallService.listProducts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<LipstickProduct> product(@PathVariable Long id) {
        LipstickProduct p = mallService.getPublicProduct(id);
        if (p == null) {
            return ApiResponse.fail("商品不存在或已下架");
        }
        return ApiResponse.ok(p);
    }

    @GetMapping("/brands")
    public ApiResponse<List<String>> brands() {
        return ApiResponse.ok(mallService.listBrands());
    }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.ok(mallService.listCategories());
    }
}
