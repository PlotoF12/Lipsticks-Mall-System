package com.example.lipsticks.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MallService {

    private final LipstickProductMapper lipstickProductMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "mall:products:";
    private static final long CACHE_TTL_MINUTES = 30;

    public Page<LipstickProduct> pageProducts(int pageNum, int pageSize,
                                               String keyword, String brand, String category) {
        boolean hasFilter = (keyword != null && !keyword.isBlank())
                || (brand != null && !brand.isBlank())
                || (category != null && !category.isBlank());

        if (!hasFilter && pageNum == 1 && pageSize == 6) {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + "first6");
            if (cached != null) {
                try {
                    return objectMapper.readValue(cached,
                            objectMapper.getTypeFactory().constructParametricType(Page.class, LipstickProduct.class));
                } catch (JsonProcessingException ignored) {
                }
            }
        }

        LambdaQueryWrapper<LipstickProduct> wrapper = Wrappers.<LipstickProduct>lambdaQuery()
                .eq(LipstickProduct::getOnSale, true);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(LipstickProduct::getTitle, keyword)
                    .or().like(LipstickProduct::getBrand, keyword)
                    .or().like(LipstickProduct::getShade, keyword)
                    .or().like(LipstickProduct::getDetail, keyword)
            );
        }
        if (brand != null && !brand.isBlank()) {
            wrapper.eq(LipstickProduct::getBrand, brand);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(LipstickProduct::getCategory, category);
        }
        wrapper.orderByAsc(LipstickProduct::getId);

        Page<LipstickProduct> page = lipstickProductMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        if (!hasFilter && pageNum == 1 && pageSize == 6) {
            try {
                redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + "first6",
                        objectMapper.writeValueAsString(page), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (JsonProcessingException ignored) {
            }
        }

        return page;
    }

    public List<LipstickProduct> listProducts() {
        return lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery().eq(LipstickProduct::getOnSale, true)
        );
    }

    public List<LipstickProduct> searchProducts(String keyword, String brand, String category) {
        LambdaQueryWrapper<LipstickProduct> wrapper = Wrappers.<LipstickProduct>lambdaQuery()
                .eq(LipstickProduct::getOnSale, true);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(LipstickProduct::getTitle, keyword)
                    .or().like(LipstickProduct::getBrand, keyword)
                    .or().like(LipstickProduct::getShade, keyword)
                    .or().like(LipstickProduct::getDetail, keyword)
            );
        }
        if (brand != null && !brand.isBlank()) {
            wrapper.eq(LipstickProduct::getBrand, brand);
        }
        if (category != null && !category.isBlank()) {
            wrapper.eq(LipstickProduct::getCategory, category);
        }
        return lipstickProductMapper.selectList(wrapper);
    }

    public LipstickProduct getPublicProduct(Long id) {
        LipstickProduct p = lipstickProductMapper.selectById(id);
        if (p == null || Boolean.FALSE.equals(p.getOnSale())) {
            return null;
        }
        return p;
    }

    public List<String> listBrands() {
        return lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery()
                        .eq(LipstickProduct::getOnSale, true)
                        .select(LipstickProduct::getBrand)
                        .groupBy(LipstickProduct::getBrand)
        ).stream().map(LipstickProduct::getBrand).filter(b -> b != null && !b.isBlank()).distinct().toList();
    }

    public List<String> listCategories() {
        return lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery()
                        .eq(LipstickProduct::getOnSale, true)
                        .select(LipstickProduct::getCategory)
                        .groupBy(LipstickProduct::getCategory)
        ).stream().map(LipstickProduct::getCategory).filter(c -> c != null && !c.isBlank()).distinct().toList();
    }
}
