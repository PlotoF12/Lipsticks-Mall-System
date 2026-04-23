package com.example.lipsticks.recommend.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.recommend.dto.AiChatRequest;
import com.example.lipsticks.recommend.dto.RecommendItem;
import com.example.lipsticks.recommend.service.AiRecommendAssistant;
import com.example.lipsticks.recommend.service.RecommendService;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;
    private final AiRecommendAssistant aiRecommendAssistant;
    private final LipstickProductMapper lipstickProductMapper;
    private final UserProfileMapper userProfileMapper;

    @GetMapping
    public ApiResponse<List<RecommendItem>> recommend(
            @RequestParam(defaultValue = "girlfriend") String target,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(recommendService.recommend(target, username));
    }

    @PostMapping("/ai")
    public ApiResponse<String> aiRecommend(@RequestBody AiChatRequest request, Authentication authentication) {
        String catalog = buildProductCatalog();
        String message = request.getMessage();

        if (authentication != null) {
            String username = authentication.getName();
            UserProfile profile = userProfileMapper.selectOne(
                    Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, username)
            );
            if (profile != null) {
                StringBuilder prefix = new StringBuilder("（用户画像：");
                if (profile.getGender() != null) prefix.append("性别=").append(profile.getGender()).append("，");
                if (profile.getSkinTone() != null) prefix.append("肤色=").append(profile.getSkinTone()).append("，");
                if (profile.getSkinType() != null) prefix.append("肤质=").append(profile.getSkinType()).append("，");
                if (prefix.charAt(prefix.length() - 1) == ',') {
                    prefix.setLength(prefix.length() - 1);
                }
                prefix.append("）");
                message = prefix + message;
            }
        }

        String reply = aiRecommendAssistant.chat(catalog, message);
        return ApiResponse.ok(reply);
    }

    private String buildProductCatalog() {
        List<LipstickProduct> products = lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery().eq(LipstickProduct::getOnSale, true)
        );

        StringBuilder sb = new StringBuilder();
        for (LipstickProduct p : products) {
            sb.append(String.format(
                    "ID:%d | %s | 品牌:%s | 色号:%s | 颜色:%s | 色系:%s | 质地:%s | 价格:%d元 | 适合肤色:%s | 适合性别:%s | 场景:%s | 详情:%s\n",
                    p.getId(),
                    p.getTitle(),
                    p.getBrand() != null ? p.getBrand() : "",
                    p.getShade() != null ? p.getShade() : "",
                    p.getColorHex() != null ? p.getColorHex() : "",
                    p.getCategory() != null ? p.getCategory() : "",
                    p.getFinishType() != null ? p.getFinishType() : "",
                    p.getPrice() != null ? p.getPrice() : 0,
                    p.getSuitableSkinTone() != null ? p.getSuitableSkinTone() : "",
                    p.getSuitableGender() != null ? p.getSuitableGender() : "",
                    p.getScene() != null ? p.getScene() : "",
                    p.getDetail() != null ? p.getDetail() : ""
            ));
        }
        return sb.toString();
    }
}
