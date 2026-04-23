package com.example.lipsticks.visualization.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.visualization.dto.ColorSwatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisualizationService {

    private final LipstickProductMapper lipstickProductMapper;

    public List<ColorSwatch> palette() {
        List<LipstickProduct> products = lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery()
                        .eq(LipstickProduct::getOnSale, true)
                        .isNotNull(LipstickProduct::getColorHex)
        );
        return products.stream()
                .map(p -> new ColorSwatch(
                        p.getId(),
                        p.getTitle(),
                        p.getBrand(),
                        p.getShade(),
                        p.getColorHex(),
                        p.getCategory()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, List<ColorSwatch>> paletteByBrand() {
        List<ColorSwatch> all = palette();
        return all.stream().collect(Collectors.groupingBy(
                s -> s.getBrand() != null ? s.getBrand() : "其他"
        ));
    }

    public Map<String, List<ColorSwatch>> paletteByCategory() {
        List<ColorSwatch> all = palette();
        return all.stream().collect(Collectors.groupingBy(
                s -> s.getCategory() != null ? s.getCategory() : "其他"
        ));
    }
}
