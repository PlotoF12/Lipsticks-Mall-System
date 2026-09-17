package com.example.lipsticks.recommend.service;

import com.example.lipsticks.recommend.dto.Conflict;
import com.example.lipsticks.recommend.dto.TemporaryIntent;
import com.example.lipsticks.user.entity.UserPreference;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 短期意图与长期偏好的融合服务。
 * <p>
 * 核心职责：
 * <ol>
 *   <li>从 ChatMemory 窗口（最近20条）中提取用户的"临时意图"</li>
 *   <li>检测临时意图与长期偏好（MySQL user_preference）之间的冲突</li>
 *   <li>生成带有优先级标注的融合上下文文本</li>
 * </ol>
 * <p>
 * 融合优先级（在 SystemMessage 中定义为规则，此处体现在标注上）：
 * <pre>
 *   P0 - 用户当前的明确要求（"我要XX"、"不要XX"）
 *   P1 - 用户近期的风格探索意图（"想尝试"、"换换风格"）
 *   P2 - 用户长期偏好（从对话中学习、持久化到 MySQL）
 *   P3 - 通用适配规则（肤色-色系、肤质-质地匹配）
 * </pre>
 * <p>
 * 设计原则：LLM 不应该自行猜测冲突处理方式——我们显式标注冲突和策略，
 * 让 LLM 从"判断者"变成"执行者"。
 */
@Service
public class PreferenceFusionService {

    /**
     * 从最近的对话消息中提取临时意图。
     * <p>
     * 使用轻量级规则匹配，不调用 LLM，保证低延迟和高确定性。
     */
    public TemporaryIntent extractTemporaryIntent(List<ChatMessage> recentMessages) {
        TemporaryIntent intent = new TemporaryIntent();

        if (recentMessages == null || recentMessages.isEmpty()) {
            return intent;
        }

        List<String> userMessages = recentMessages.stream()
                .filter(m -> m instanceof UserMessage)
                .map(m -> ((UserMessage) m).singleText())
                .filter(t -> t != null && !t.isBlank())
                .toList();

        // 只分析最近 5 条用户发言
        List<String> recent = userMessages.size() > 5
                ? userMessages.subList(userMessages.size() - 5, userMessages.size())
                : userMessages;

        for (String msg : recent) {
            // 检测"尝试新风格"信号
            if (matchesAny(msg, "想试", "想尝试", "换换", "腻了", "太无聊", "不一样的", "改变一下", "换个")) {
                intent.setExploreNew(true);
            }

            // 检测明确的色系要求
            if (matchesAny(msg, "紫色", "purple")) intent.getRequestedColors().add("purple");
            if (matchesAny(msg, "红色", "正红", "red")) intent.getRequestedColors().add("red");
            if (matchesAny(msg, "粉色", "pink")) intent.getRequestedColors().add("pink");
            if (matchesAny(msg, "裸色", "nude", "豆沙", "低调", "自然")) intent.getRequestedColors().add("nude");
            if (matchesAny(msg, "橘色", "橙色", "orange")) intent.getRequestedColors().add("orange");

            // 检测场景变化
            if (matchesAny(msg, "日常", "上班", "通勤", "素颜")) intent.setScene("日常");
            if (matchesAny(msg, "约会", "约会", "对象")) intent.setScene("约会");
            if (matchesAny(msg, "聚会", "party", "派对", "蹦迪")) intent.setScene("聚会");
            if (matchesAny(msg, "送礼", "礼物", "送")) intent.setScene("送礼");

            // 检测明确排除（"不要"、"不喜欢"）
            if (msg.contains("不要") || msg.contains("不想") || msg.contains("别推")) {
                intent.setHasExplicitExclusion(true);
            }
        }

        return intent;
    }

    /**
     * 检测短期意图与长期偏好之间的冲突。
     *
     * @param longTermPrefs 长期偏好（可为 null）
     * @param tempIntent 临时意图
     * @return 冲突列表
     */
    public List<Conflict> detectConflicts(UserPreference longTermPrefs, TemporaryIntent tempIntent) {
        List<Conflict> conflicts = new ArrayList<>();

        if (longTermPrefs == null) {
            return conflicts;
        }

        // 冲突1: 色系矛盾 —— 用户临时要的颜色在长期不喜欢的列表中
        if (longTermPrefs.getDislikedColors() != null) {
            String[] disliked = longTermPrefs.getDislikedColors().split(",");
            for (String requested : tempIntent.getRequestedColors()) {
                for (String d : disliked) {
                    if (d.trim().equalsIgnoreCase(requested.trim())) {
                        conflicts.add(new Conflict(
                                "COLOR_CONTRADICTION",
                                "用户长期不偏好 " + requested + " 色系，但当前明确要尝试 " + requested + " 色系",
                                "CURRENT_EXPLICIT_REQUEST",
                                "以用户当前的明确要求为准，忽略长期偏好中的不偏好记录"
                        ));
                    }
                }
            }
        }

        // 冲突2: 探索意图 vs 长期偏好 —— 用户想探索新风格
        if (tempIntent.isExploreNew() && longTermPrefs.getPreferredColors() != null) {
            conflicts.add(new Conflict(
                    "EXPLORE_VS_HABIT",
                    "用户表达了尝试新风格的意愿，与长期偏好 "
                            + longTermPrefs.getPreferredColors() + " 可能存在张力",
                    "CURRENT_EXPLICIT_REQUEST",
                    "优先推荐新风格，但可提及长期偏好中仍适用的款式作为备选"
            ));
        }

        return conflicts;
    }

    /**
     * 构建注入 UserMessage 的偏好上下文文本。
     * <p>
     * 位置选择：UserMessage（非 SystemMessage），原因是：
     * <ul>
     *   <li>SystemMessage 中的内容会被 LLM 理解为"不可违背的规则"</li>
     *   <li>UserMessage 中的内容会被 LLM 理解为"可被当前需求覆盖的数据"</li>
     *   <li>避免 LLM 对长期偏好产生"指令惯性"</li>
     * </ul>
     */
    public String buildPreferenceContext(UserPreference longTermPrefs, TemporaryIntent tempIntent,
                                          List<Conflict> conflicts) {
        StringBuilder sb = new StringBuilder();
        sb.append("【用户背景】");

        boolean hasContent = false;

        // 长期偏好
        if (longTermPrefs != null) {
            sb.append("\n长期偏好：");
            if (longTermPrefs.getPreferredFinish() != null) {
                sb.append("偏爱").append(finishLabel(longTermPrefs.getPreferredFinish())).append("质地，");
                hasContent = true;
            }
            if (longTermPrefs.getPreferredColors() != null) {
                sb.append("常选色系").append(longTermPrefs.getPreferredColors()).append("，");
                hasContent = true;
            }
            if (longTermPrefs.getPreferredBrands() != null) {
                sb.append("常购品牌").append(longTermPrefs.getPreferredBrands()).append("，");
                hasContent = true;
            }
            if (longTermPrefs.getDislikedColors() != null) {
                sb.append("不偏好").append(longTermPrefs.getDislikedColors()).append("，");
                hasContent = true;
            }
            if (longTermPrefs.getLipCondition() != null) {
                sb.append("唇部").append(lipLabel(longTermPrefs.getLipCondition())).append("，");
                hasContent = true;
            }
            if (sb.charAt(sb.length() - 1) == '，') {
                sb.setLength(sb.length() - 1);
            }
        }

        // 近期意图
        if (tempIntent.hasContent()) {
            sb.append("\n近期意图：");
            if (tempIntent.isExploreNew()) sb.append("探索新风格，");
            if (!tempIntent.getRequestedColors().isEmpty())
                sb.append("明确要求").append(String.join("、", tempIntent.getRequestedColors())).append("色系，");
            if (tempIntent.getScene() != null)
                sb.append("场景为").append(tempIntent.getScene());
            if (sb.charAt(sb.length() - 1) == '，') {
                sb.setLength(sb.length() - 1);
            }
            hasContent = true;
        }

        // 冲突标注
        if (!conflicts.isEmpty()) {
            sb.append("\n冲突已标注：");
            for (Conflict c : conflicts) {
                sb.append("\n  ⚠ ").append(c.getType())
                        .append(" → 处理策略: ").append(c.getResolutionStrategy());
            }
            hasContent = true;
        }

        if (!hasContent) {
            sb.append("\n暂无历史偏好数据");
        }

        return sb.toString();
    }

    /**
     * 构建包含冲突处理规则的 SystemMessage 部分。
     * <p>
     * 位置选择：拼接到 @SystemMessage 中，使优先级规则对 LLM 具有约束力。
     */
    public String buildSystemEnhancement(List<Conflict> conflicts) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        sb.append("【推荐决策优先级】（从高到低，严格遵守）\n");
        sb.append("P0 - 用户当前的明确要求：如果用户本轮对话中明确说了\"要XX\"、\"不要XX\"，必须严格遵守，不可被任何历史偏好覆盖\n");
        sb.append("P1 - 用户近期的风格探索意图：如果用户表达了\"想尝试\"、\"换换风格\"，优先推荐新风格\n");
        sb.append("P2 - 用户长期偏好：在无冲突时作为默认推荐依据，有冲突时降级为参考\n");
        sb.append("P3 - 通用适配规则：肤色-色系匹配、肤质-质地匹配\n");

        if (!conflicts.isEmpty()) {
            sb.append("\n【当前冲突提示】（已为你标注处理策略，请严格遵守）\n");
            for (Conflict c : conflicts) {
                sb.append("⚠ ").append(c.getDescription()).append("\n");
                sb.append("  处理策略：").append(c.getResolutionStrategy()).append("\n");
                sb.append("  具体指引：").append(c.getGuidance()).append("\n");
            }
        }

        return sb.toString();
    }

    // ==================== 辅助方法 ====================

    private boolean matchesAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String finishLabel(String finish) {
        return switch (finish.trim().toLowerCase()) {
            case "matte" -> "哑光";
            case "gloss" -> "滋润";
            case "satin" -> "缎面";
            default -> finish;
        };
    }

    private String lipLabel(String condition) {
        return switch (condition.trim().toLowerCase()) {
            case "dry" -> "偏干";
            case "normal" -> "正常";
            default -> condition;
        };
    }
}
