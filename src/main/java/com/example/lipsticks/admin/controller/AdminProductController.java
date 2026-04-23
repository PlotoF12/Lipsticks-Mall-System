package com.example.lipsticks.admin.controller;

import com.example.lipsticks.admin.dto.AdminProductUpsertRequest;
import com.example.lipsticks.admin.service.AdminProductService;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.mall.entity.LipstickProduct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping
    public ApiResponse<List<LipstickProduct>> list() {
        return ApiResponse.ok(adminProductService.listAll());
    }

    @PostMapping
    public ApiResponse<LipstickProduct> create(@Valid @RequestBody AdminProductUpsertRequest request) {
        try {
            return ApiResponse.ok(adminProductService.create(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<LipstickProduct> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminProductUpsertRequest request
    ) {
        try {
            return ApiResponse.ok(adminProductService.update(id, request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
}
