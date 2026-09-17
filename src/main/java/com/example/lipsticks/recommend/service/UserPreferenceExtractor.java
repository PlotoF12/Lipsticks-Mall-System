package com.example.lipsticks.recommend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.user.entity.UserPreference;
import com.example.lipsticks.user.mapper.UserPreferenceMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户偏好提取器 —— 从对话历史中异步提取结构化偏好。
 * <p>
 * 触发时机：每次 AI 对话结束后异步执行。<br>
 * 提取方式：用 LLM 分析对话，提取用户明确表达的偏好，结构化存储到 MySQL。
 * <p>
 * 为什么异步？
 * <ul>
 *   <li>偏好提取不是推荐的必要前提——本次推荐已经完成</li>
 *   <li>LLM 调用有延迟（~1-3s），同步等待会让用户多等</li>
 *   <li>提取失败不应影响推荐主流程</li>
 * </ul>
 * <p>
 * 为什么用 LLM 而不是规则？
 * <ul>
 *   <li>"我喜欢哑光的，Tom Ford 那种感觉的" —— 规则难以解析这种隐含表达</li>
 *   <li>"之前买过 MAC 的小辣椒还不错" —— 需要语义理解才能提取出"品牌偏好 MAC"</li>
 *   <li>LLM 做信息提取任务成本低（few-shot prompt，输出 JSON）</li>
 * </ul>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferenceExtractor {

    private final ChatLanguageModel chatLanguageModel;
    private final UserPreferenceMapper preferenceMapper;

    /**
     * 从对话历史中提取用户偏好，异步执行。
     *
     * @param username     用户名（登录用户才有意义）
     * @param conversation 对话历史消息列表
     */
    @Async
    public void extractAndSave(String username, List<ChatMessage> conversation) {
        if (username == null || conversation == null || conversation.size() < 2) {
            return;
        }

        try {
            String conversationText = conversation.stream()
                    .map(m -> String.format("[%s] %s", m.type().name(), m.toString()))
                    .collect(Collectors.joining("\n"));

            String prompt = buildExtractionPrompt(conversationText);
            String extracted = chatLanguageModel.generate(prompt);

            UserPreference prefs = parseExtractionResult(extracted);
            if (prefs != null && hasContent(prefs)) {
                saveOrUpdate(username, prefs);
            }
        } catch (Exception e) {
            log.warn("偏好提取失败 username={}, 不影响主流程", username, e);
            // 静默失败——偏好提取是锦上添花，不应影响用户体验
        }
    }

    private String buildExtractionPrompt(String conversation) {
        return """
            从以下用户与口红推荐助手的对话中，提取用户对口红的偏好，只提取用户明确表达或强烈暗示的偏好，不要猜测。
            返回严格的JSON格式，不要输出其他内容。

            {
              "preferredColors": "逗号分隔的色系 (red/pink/nude/bean_paste/rose/tomato/red_brown/rose_brown)，未提到则为null",
              "preferredFinish": "偏好的质地 (matte/gloss/satin)，未提到则为null",
              "preferredBrands": "逗号分隔的品牌 (Dior/YSL/Armani/Chanel/MAC/Tom Ford/NARS)，未提到则为null",
              "priceMin": 价格下限整数（元），未提到则为null,
              "priceMax": 价格上限整数（元），未提到则为null,
              "dislikedColors": "逗号分隔的不喜欢的色系，未提到则为null",
              "scenes": "逗号分隔的常用场景 (日常/通勤/约会/聚会/送礼)，未提到则为null",
              "lipCondition": "唇部状况 (dry/normal)，未提到则为null
            }

            对话：
            """ + conversation;
    }

    private UserPreference parseExtractionResult(String raw) {
        try {
            // 提取 JSON 块（LLM 可能在外面包裹 markdown 代码块）
            String json = raw;
            if (json.contains("```")) {
                int start = json.indexOf("{");
                int end = json.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    json = json.substring(start, end);
                }
            }

            // 使用简单的字符串解析（避免引入额外 JSON 库依赖）
            return parsePreferenceJson(json);
        } catch (Exception e) {
            log.warn("偏好提取JSON解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 简化 JSON 解析 —— 只提取我们关心的字段。
     * 生产环境应使用 Jackson/Gson，此处为避免新增依赖采用简单解析。
     */
    private UserPreference parsePreferenceJson(String json) {
        UserPreference p = new UserPreference();
        p.setPreferredColors(extractString(json, "preferredColors"));
        p.setPreferredFinish(extractString(json, "preferredFinish"));
        p.setPreferredBrands(extractString(json, "preferredBrands"));
        p.setPriceMin(extractInt(json, "priceMin"));
        p.setPriceMax(extractInt(json, "priceMax"));
        p.setDislikedColors(extractString(json, "dislikedColors"));
        p.setScenes(extractString(json, "scenes"));
        p.setLipCondition(extractString(json, "lipCondition"));
        return p;
    }

    private String extractString(String json, String key) {
        int keyIdx = json.indexOf("\"" + key + "\"");
        if (keyIdx < 0) return null;
        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx < 0) return null;
        // 跳过冒号和空格/null
        String after = json.substring(colonIdx + 1).trim();
        if (after.startsWith("null")) return null;
        if (after.startsWith("\"")) {
            int endQuote = after.indexOf("\"", 1);
            if (endQuote > 0) {
                String val = after.substring(1, endQuote).trim();
                return val.isEmpty() || "null".equals(val) ? null : val;
            }
        }
        return null;
    }

    private Integer extractInt(String json, String key) {
        String val = extractString(json, key);
        if (val == null) return null;
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasContent(UserPreference p) {
        return p.getPreferredColors() != null
                || p.getPreferredFinish() != null
                || p.getPreferredBrands() != null
                || p.getDislikedColors() != null
                || p.getScenes() != null
                || p.getLipCondition() != null;
    }

    private void saveOrUpdate(String username, UserPreference newPrefs) {
        UserPreference existing = preferenceMapper.selectOne(
                Wrappers.<UserPreference>lambdaQuery().eq(UserPreference::getUsername, username)
        );

        newPrefs.setUsername(username);
        newPrefs.setUpdatedAt(LocalDateTime.now());

        if (existing != null) {
            // merge：新的不为 null 就覆盖旧的
            if (newPrefs.getPreferredColors() != null)
                existing.setPreferredColors(newPrefs.getPreferredColors());
            if (newPrefs.getPreferredFinish() != null)
                existing.setPreferredFinish(newPrefs.getPreferredFinish());
            if (newPrefs.getPreferredBrands() != null)
                existing.setPreferredBrands(newPrefs.getPreferredBrands());
            if (newPrefs.getPriceMin() != null) existing.setPriceMin(newPrefs.getPriceMin());
            if (newPrefs.getPriceMax() != null) existing.setPriceMax(newPrefs.getPriceMax());
            if (newPrefs.getDislikedColors() != null)
                existing.setDislikedColors(newPrefs.getDislikedColors());
            if (newPrefs.getScenes() != null) existing.setScenes(newPrefs.getScenes());
            if (newPrefs.getLipCondition() != null)
                existing.setLipCondition(newPrefs.getLipCondition());
            existing.setUpdatedAt(LocalDateTime.now());
            preferenceMapper.updateById(existing);
            log.info("用户偏好已更新: username={}", username);
        } else {
            preferenceMapper.insert(newPrefs);
            log.info("用户偏好已创建: username={}", username);
        }
    }
}
