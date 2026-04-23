package com.example.lipsticks.admin.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.admin.dto.AdminProductUpsertRequest;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final LipstickProductMapper lipstickProductMapper;

    public List<LipstickProduct> listAll() {
        return lipstickProductMapper.selectList(Wrappers.emptyWrapper());
    }

    public LipstickProduct create(AdminProductUpsertRequest request) {
        LipstickProduct p = toEntity(request, null);
        lipstickProductMapper.insert(p);
        return p;
    }

    public LipstickProduct update(Long id, AdminProductUpsertRequest request) {
        LipstickProduct existing = lipstickProductMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        LipstickProduct p = toEntity(request, existing);
        p.setId(id);
        lipstickProductMapper.updateById(p);
        return p;
    }

    private static LipstickProduct toEntity(AdminProductUpsertRequest request, LipstickProduct base) {
        LipstickProduct p = base == null ? new LipstickProduct() : base;
        p.setTitle(request.getTitle());
        p.setBrand(request.getBrand());
        p.setShade(request.getShade());
        p.setColorHex(request.getColorHex());
        p.setCategory(request.getCategory());
        p.setFinishType(request.getFinishType());
        p.setDetail(request.getDetail());
        p.setPrice(request.getPrice());
        p.setStock(request.getStock());
        p.setOnSale(request.getOnSale());
        p.setSuitableSkinTone(request.getSuitableSkinTone());
        p.setSuitableGender(request.getSuitableGender());
        p.setScene(request.getScene());
        p.setImageUrl(request.getImageUrl());
        return p;
    }
}
