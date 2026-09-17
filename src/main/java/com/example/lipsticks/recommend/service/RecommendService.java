package com.example.lipsticks.recommend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.recommend.dto.RecommendItem;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final LipstickProductMapper lipstickProductMapper;
    private final UserProfileMapper userProfileMapper;

    public List<RecommendItem> recommend(String target, String username) {
        UserProfile profile = null;
        if (username != null) {
            profile = userProfileMapper.selectOne(
                    Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, username)
            );
        }

        List<LipstickProduct> allOnSale = lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery().eq(LipstickProduct::getOnSale, true)
        );

        if ("self".equalsIgnoreCase(target)) {
            return recommendForSelf(allOnSale, profile);
        }
        return recommendForGirlfriend(allOnSale, profile);
    }

    private List<RecommendItem> recommendForGirlfriend(List<LipstickProduct> products, UserProfile maleProfile) {
        String targetSkinTone = inferFemaleSkinTone(maleProfile);
        List<ScoredProduct> scored = products.stream()
                .filter(p -> p.getSuitableGender() != null && p.getSuitableGender().contains("female"))
                .map(p -> {
                    int score = computeFemaleScore(p, targetSkinTone);
                    String reason = buildGirlfriendReason(p, targetSkinTone);
                    return new ScoredProduct(p, score, reason);
                })
                .sorted(Comparator.comparingInt(ScoredProduct::score).reversed())
                .limit(6)
                .toList();

        return scored.stream()
                .map(sp -> toRecommendItem(sp.product, sp.reason))
                .collect(Collectors.toList());
    }

    private List<RecommendItem> recommendForSelf(List<LipstickProduct> products, UserProfile maleProfile) {
        String skinTone = maleProfile != null ? maleProfile.getSkinTone() : null;
        List<ScoredProduct> scored = products.stream()
                .filter(p -> p.getSuitableGender() != null && p.getSuitableGender().contains("male"))
                .map(p -> {
                    int score = computeMaleScore(p, skinTone);
                    String reason = buildSelfReason(p, skinTone);
                    return new ScoredProduct(p, score, reason);
                })
                .sorted(Comparator.comparingInt(ScoredProduct::score).reversed())
                .limit(6)
                .toList();

        return scored.stream()
                .map(sp -> toRecommendItem(sp.product, sp.reason))
                .collect(Collectors.toList());
    }

    private int computeFemaleScore(LipstickProduct p, String targetSkinTone) {
        int score = 0;
        if (p.getSuitableSkinTone() != null && targetSkinTone != null) {
            if (p.getSuitableSkinTone().contains(targetSkinTone)) {
                score += 30;
            }
        }
        if (p.getSuitableSkinTone() != null && p.getSuitableSkinTone().contains("warm")
                && p.getSuitableSkinTone().contains("neutral") && p.getSuitableSkinTone().contains("cool")) {
            score += 15;
        }
        if (p.getScene() != null) {
            if (p.getScene().contains("送礼")) score += 20;
            if (p.getScene().contains("约会")) score += 15;
            if (p.getScene().contains("日常")) score += 10;
        }
        if ("red".equals(p.getCategory()) || "tomato".equals(p.getCategory())) {
            score += 10;
        }
        if (p.getPrice() != null && p.getPrice() >= 300) {
            score += 5;
        }
        return score;
    }

    private int computeMaleScore(LipstickProduct p, String skinTone) {
        int score = 0;
        if (p.getSuitableSkinTone() != null && skinTone != null) {
            if (p.getSuitableSkinTone().contains(skinTone)) {
                score += 30;
            }
        }
        if (p.getSuitableSkinTone() != null && p.getSuitableSkinTone().contains("warm")
                && p.getSuitableSkinTone().contains("neutral")) {
            score += 10;
        }
        if ("nude".equals(p.getCategory())) {
            score += 25;
        }
        if ("bean_paste".equals(p.getCategory())) {
            score += 15;
        }
        if (p.getScene() != null) {
            if (p.getScene().contains("男性")) score += 20;
            if (p.getScene().contains("日常")) score += 10;
            if (p.getScene().contains("通勤")) score += 10;
        }
        if ("matte".equals(p.getFinishType()) || "satin".equals(p.getFinishType())) {
            score += 5;
        }
        return score;
    }

    private String inferFemaleSkinTone(UserProfile maleProfile) {
        if (maleProfile == null || maleProfile.getSkinTone() == null) {
            return "neutral";
        }
        return switch (maleProfile.getSkinTone().toLowerCase()) {
            case "warm" -> "warm";
            case "cool" -> "cool";
            default -> "neutral";
        };
    }

    private String buildGirlfriendReason(LipstickProduct p, String targetSkinTone) {
        List<String> parts = new ArrayList<>();
        if (p.getScene() != null && p.getScene().contains("送礼")) {
            parts.add("送礼不易踩雷");
        } else {
            parts.add("适配多数女性肤色");
        }
        if (p.getSuitableSkinTone() != null && targetSkinTone != null
                && p.getSuitableSkinTone().contains(targetSkinTone)) {
            parts.add(skinToneLabel(targetSkinTone) + "友好");
        }
        if (p.getScene() != null) {
            if (p.getScene().contains("约会")) parts.add("约会场景出色");
            if (p.getScene().contains("日常")) parts.add("日常百搭");
            if (p.getScene().contains("聚会")) parts.add("聚会气场足");
        }
        if ("red".equals(p.getCategory())) parts.add("经典正红显白");
        if ("tomato".equals(p.getCategory())) parts.add("番茄色元气减龄");
        if ("bean_paste".equals(p.getCategory())) parts.add("豆沙色温柔知性");
        if (parts.isEmpty()) parts.add("口碑好款，值得入手");
        return p.getTitle() + "——" + String.join("，", parts);
    }

    private String buildSelfReason(LipstickProduct p, String skinTone) {
        List<String> parts = new ArrayList<>();
        parts.add("男性日常场景友好");
        if ("nude".equals(p.getCategory())) {
            parts.add("低饱和裸色自然不突兀");
        }
        if ("bean_paste".equals(p.getCategory())) {
            parts.add("豆沙色低调提气色");
        }
        if (p.getSuitableSkinTone() != null && skinTone != null
                && p.getSuitableSkinTone().contains(skinTone)) {
            parts.add(skinToneLabel(skinTone) + "适配");
        }
        if (p.getScene() != null) {
            if (p.getScene().contains("通勤")) parts.add("通勤零压力");
            if (p.getScene().contains("日常")) parts.add("日常自然");
        }
        if ("matte".equals(p.getFinishType())) parts.add("哑光质感更显沉稳");
        if ("satin".equals(p.getFinishType())) parts.add("缎面质感低调有型");
        if (parts.size() == 1) parts.add("提气色但不张扬");
        return p.getTitle() + "——" + String.join("，", parts);
    }

    private String skinToneLabel(String skinTone) {
        return switch (skinTone.toLowerCase()) {
            case "warm" -> "暖皮";
            case "cool" -> "冷皮";
            default -> "中性皮";
        };
    }

    private RecommendItem toRecommendItem(LipstickProduct p, String reason) {
        return new RecommendItem(
                p.getId(),
                p.getTitle(),
                p.getBrand(),
                p.getShade(),
                p.getColorHex(),
                p.getCategory(),
                p.getPrice(),
                reason
        );
    }

    private record ScoredProduct(LipstickProduct product, int score, String reason) {
    }
}
