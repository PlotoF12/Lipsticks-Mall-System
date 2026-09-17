package com.example.lipsticks.visualization.controller;

import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.visualization.dto.ColorSwatch;
import com.example.lipsticks.visualization.service.VisualizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visualization")
@RequiredArgsConstructor
public class VisualizationController {

    private final VisualizationService visualizationService;

    @GetMapping("/palette")
    public ApiResponse<List<ColorSwatch>> palette() {
        return ApiResponse.ok(visualizationService.palette());
    }

    @GetMapping("/palette/by-brand")
    public ApiResponse<Map<String, List<ColorSwatch>>> paletteByBrand() {
        return ApiResponse.ok(visualizationService.paletteByBrand());
    }

    @GetMapping("/palette/by-category")
    public ApiResponse<Map<String, List<ColorSwatch>>> paletteByCategory() {
        return ApiResponse.ok(visualizationService.paletteByCategory());
    }
}
