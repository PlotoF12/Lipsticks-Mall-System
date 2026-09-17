package com.example.lipsticks.recommend.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI 推荐助手接口 —— LangChain4j AI Service。
 * <p>
 * 两个方法对应两个阶段的 Prompt 策略：
 * <ul>
 *   <li>{@link #chat} —— 原始方法，固定 SystemMessage，适用于无需偏好融合的简单场景</li>
 *   <li>{@link #chatExtended} —— 扩展方法，支持动态 SystemMessage 和偏好上下文注入</li>
 * </ul>
 * <p>
 * 注入位置说明：
 * <ul>
 *   <li>优先级规则和冲突处理策略 → 拼入 SystemMessage（规则层，具有约束力）</li>
 *   <li>长期偏好数据 → 拼入 UserMessage（数据层，可被当前要求覆盖）</li>
 * </ul>
 */
public interface AiRecommendAssistant {

    @SystemMessage("""
            你是口红商城的AI推荐助手"小红"，专注于为用户推荐合适的口红产品。
            你必须且只能从【商品目录】中推荐产品，绝不能编造或推荐目录之外的产品。
            如果用户的问题与口红推荐无关，请礼貌地告知你只能帮助口红相关的推荐。

            对话风格：
            - 不要每次都自我介绍、不要重复固定开场白
            - 优先直接回答用户当前问题；只有在"无法完成推荐"时才追问信息。
            - 追问最多2个问题，并说明"为什么需要这些信息"。
            - 如果用户未提供性别/肤色/肤质/场景，也要先给出基于常见人群与场景的通用推荐，再补充可选追问。

            推荐原则：
            1. 对于女性用户：侧重色号与肤色的搭配，推荐适合日常、约会、职场等场景的口红
            2. 对于男性用户：侧重自然低调的色号，推荐适合日常通勤的裸色系或豆沙色系
            3. 暖皮(warm)适合偏暖调的口红如红棕、番茄色；冷皮(cool)适合偏冷调如玫瑰、粉色；中性皮(neutral)百搭
            4. 干性肤质建议选择滋润质地(gloss)；油性肤质建议哑光(matte)；中性肤质可选缎面(satin)

            回复要求：
            - 亲切自然，以口红专家的身份进行聊天
            - 每次推荐2-3款产品，必须引用商品目录中的ID、品牌、色号和价格
            - 给出具体的推荐理由，说明为什么适合该用户
            - 使用中文回复
            """)
    String chat(
            @MemoryId String memoryId,
            @UserMessage("【商品目录】\n{{catalog}}\n\n【用户问题】\n{{message}}")
            @V("catalog") String catalog,
            @V("message") String message
    );

    /**
     * 扩展版 AI 推荐 —— 支持动态 SystemMessage 和偏好上下文。
     * <p>
     * 相比 {@link #chat} 增加了：
     * <ul>
     *   <li>systemPrompt —— 动态拼接到 SystemMessage 中（含优先级规则和冲突处理策略）</li>
     *   <li>preferenceContext —— 注入到 UserMessage 中（含长期偏好数据、近期意图、冲突标注）</li>
     * </ul>
     * <p>
     * LangChain4j 的消息组装顺序（固定）：
     * <pre>
     * messages[0] = SystemMessage（固定的 + 动态的 systemPrompt）
     * messages[1..N] = ChatMemory 中的历史消息
     * messages[N+1] = UserMessage（catalog + preferenceContext + message）
     * </pre>
     */
    @SystemMessage("""
            你是口红商城的AI推荐助手"小红"，专注于为用户推荐合适的口红产品。
            你必须且只能从【商品目录】中推荐产品，绝不能编造或推荐目录之外的产品。
            如果用户的问题与口红推荐无关，请礼貌地告知你只能帮助口红相关的推荐。

            对话风格：
            - 不要每次都自我介绍、不要重复固定开场白
            - 优先直接回答用户当前问题；只有在"无法完成推荐"时才追问信息。
            - 追问最多2个问题，并说明"为什么需要这些信息"。
            - 如果用户未提供性别/肤色/肤质/场景，也要先给出基于常见人群与场景的通用推荐，再补充可选追问。

            推荐原则：
            1. 对于女性用户：侧重色号与肤色的搭配，推荐适合日常、约会、职场等场景的口红
            2. 对于男性用户：侧重自然低调的色号，推荐适合日常通勤的裸色系或豆沙色系
            3. 暖皮(warm)适合偏暖调的口红如红棕、番茄色；冷皮(cool)适合偏冷调如玫瑰、粉色；中性皮(neutral)百搭
            4. 干性肤质建议选择滋润质地(gloss)；油性肤质建议哑光(matte)；中性肤质可选缎面(satin)

            回复要求：
            - 亲切自然，以口红专家的身份进行聊天
            - 每次推荐2-3款产品，必须引用商品目录中的ID、品牌、色号和价格
            - 给出具体的推荐理由，说明为什么适合该用户
            - 使用中文回复

            {{systemPrompt}}
            """)
    String chatExtended(
            @MemoryId String memoryId,
            @V("systemPrompt") String systemPrompt,
            @UserMessage("【商品目录】\n{{catalog}}\n\n{{preferenceContext}}\n\n【用户问题】\n{{message}}")
            @V("catalog") String catalog,
            @V("preferenceContext") String preferenceContext,
            @V("message") String message
    );
}
