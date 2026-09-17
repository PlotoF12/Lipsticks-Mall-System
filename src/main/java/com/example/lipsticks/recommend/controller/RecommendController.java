package com.example.lipsticks.recommend.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.recommend.dto.AiChatRequest;
import com.example.lipsticks.recommend.dto.Conflict;
import com.example.lipsticks.recommend.dto.RecommendItem;
import com.example.lipsticks.recommend.dto.TemporaryIntent;
import com.example.lipsticks.recommend.service.AiRecommendService;
import com.example.lipsticks.recommend.service.PreferenceFusionService;
import com.example.lipsticks.recommend.service.RecommendService;
import com.example.lipsticks.recommend.service.UserPreferenceExtractor;
import com.example.lipsticks.user.entity.UserPreference;
import com.example.lipsticks.user.entity.UserProfile;
import com.example.lipsticks.user.mapper.UserPreferenceMapper;
import com.example.lipsticks.user.mapper.UserProfileMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
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
    private final AiRecommendService aiRecommendService;
    private final PreferenceFusionService preferenceFusionService;
    private final UserPreferenceExtractor preferenceExtractor;
    private final ChatMemoryStore chatMemoryStore;
    private final LipstickProductMapper lipstickProductMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserPreferenceMapper userPreferenceMapper;

    @GetMapping
    public ApiResponse<List<RecommendItem>> recommend(
            @RequestParam(defaultValue = "girlfriend") String target,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ApiResponse.ok(recommendService.recommend(target, username));
    }

    /**
     * AI 对话推荐 —— 完整 RAG 链路。
     * <p>
     * 处理流程：
     * <ol>
     *   <li>构建商品目录（全量注入 —— 因数据量仅 16 条）</li>
     *   <li>提取长期偏好（MySQL）和短期意图（ChatMemory）</li>
     *   <li>检测冲突，生成优先级规则和上下文</li>
     *   <li>调用 LLM（含重试）</li>
     *   <li>异步提取用户偏好（从本次对话中学习）</li>
     * </ol>
     */
    @PostMapping("/ai")
    public ApiResponse<String> aiRecommend(@RequestBody AiChatRequest request,
                                            Authentication authentication) {
        // Step 1: 构建商品目录
        String catalog = buildProductCatalog();

        // Step 2: 构建记忆键
        String memoryId = buildMemoryId(authentication, request);
        String username = authentication != null ? authentication.getName() : null;

        // Step 3: 构建用户消息（含画像信息）
        String enhancedMessage = buildEnhancedMessage(request.getMessage(), username);

        // Step 4: 提取长期偏好和短期意图
        UserPreference longTermPrefs = null;
        if (username != null) {
            longTermPrefs = userPreferenceMapper.selectOne(
                    Wrappers.<UserPreference>lambdaQuery()
                            .eq(UserPreference::getUsername, username));
        }

        List<ChatMessage> recentMessages = chatMemoryStore.getMessages(memoryId);
        TemporaryIntent tempIntent = preferenceFusionService.extractTemporaryIntent(recentMessages);
        List<Conflict> conflicts = preferenceFusionService.detectConflicts(longTermPrefs, tempIntent);

        // Step 5: 构建融合上下文
        String systemEnhancement = preferenceFusionService.buildSystemEnhancement(conflicts);
        String preferenceContext = preferenceFusionService.buildPreferenceContext(
                longTermPrefs, tempIntent, conflicts);

        // Step 6: AI 推荐（含重试）
        String reply = aiRecommendService.chatWithRetry(
                memoryId, systemEnhancement, catalog, preferenceContext, enhancedMessage);

        // Step 7: 异步提取用户偏好（从本次对话中学习）
        if (username != null) {
            List<ChatMessage> updatedMessages = chatMemoryStore.getMessages(memoryId);
            preferenceExtractor.extractAndSave(username, updatedMessages);
        }

        return ApiResponse.ok(reply);
    }

    /**
     * 构建增强的用户消息 —— 附加上用户画像信息。
     */
    private String buildEnhancedMessage(String rawMessage, String username) {
        if (username == null) return rawMessage;

        UserProfile profile = userProfileMapper.selectOne(
                Wrappers.<UserProfile>lambdaQuery().eq(UserProfile::getUsername, username));

        if (profile == null) return rawMessage;

        StringBuilder prefix = new StringBuilder("（用户画像：");
        boolean hasInfo = false;
        if (profile.getGender() != null) {
            prefix.append("性别=").append(profile.getGender());
            hasInfo = true;
        }
        if (profile.getSkinTone() != null) {
            if (hasInfo) prefix.append("，");
            prefix.append("肤色=").append(profile.getSkinTone());
            hasInfo = true;
        }
        if (profile.getSkinType() != null) {
            if (hasInfo) prefix.append("，");
            prefix.append("肤质=").append(profile.getSkinType());
        }
        prefix.append("）");
        return prefix + rawMessage;
    }

    private String buildMemoryId(Authentication authentication, AiChatRequest request) {
        if (authentication != null) {
            return "user:" + authentication.getName();
        }
        String conversationId = request.getConversationId();
        return "conv:" + (conversationId != null && !conversationId.isBlank()
                ? conversationId : "anonymous");
    }

    private String buildProductCatalog() {
        List<LipstickProduct> products = lipstickProductMapper.selectList(
                Wrappers.<LipstickProduct>lambdaQuery().eq(LipstickProduct::getOnSale, true));

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
                    p.getDetail() != null ? p.getDetail() : ""));
        }
        return sb.toString();
    }
}
