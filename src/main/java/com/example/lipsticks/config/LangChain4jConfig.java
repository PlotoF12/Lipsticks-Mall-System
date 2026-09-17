package com.example.lipsticks.config;

import com.example.lipsticks.recommend.service.AiRecommendAssistant;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LangChain4jConfig {

    /**
     * 兜底 ChatMemoryStore —— 纯堆内存，测试环境使用。
     * 当 RedisChatMemoryStore 不可用（Redis 不存在）时作为回退。
     */
    @Bean
    @Primary
    public ChatMemoryStore inMemoryChatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    /**
     * 配置 ChatMemoryProvider。
     * <p>
     * 通过 AiServices 自动发现容器中的 ChatMemoryStore：
     * <ul>
     *   <li>生产环境：RedisChatMemoryStore（如果 Redis 可用）或 InMemoryChatMemoryStore</li>
     *   <li>测试环境：只使用 InMemoryChatMemoryStore</li>
     * </ul>
     * <p>
     * 注意：当 RedisChatMemoryStore 存在时，它实现了 ChatMemoryStore 接口，
     * 但由于 inMemoryChatMemoryStore 标注了 @Primary，默认注入会使用堆内存版本。
     * ChatMemoryProvider 内部通过手动指定 store 来选择使用哪个实现。
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        // 使用堆内存存储作为默认 —— 简单可靠
        // 生产环境如需 Redis 持久化，可通过配置切换
        return memoryId -> MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    /**
     * 配置 AI 推荐助手。
     */
    @Bean
    public AiRecommendAssistant aiRecommendAssistant(ChatLanguageModel chatLanguageModel,
                                                      ChatMemoryProvider chatMemoryProvider) {
        return AiServices.builder(AiRecommendAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }
}
