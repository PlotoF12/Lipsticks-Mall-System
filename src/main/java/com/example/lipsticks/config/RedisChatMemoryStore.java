package com.example.lipsticks.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 持久化的 ChatMemoryStore 实现。
 * <p>
 * 解决 LangChain4j 默认堆内存存储的两个问题：
 * <ol>
 *   <li>服务重启后对话历史丢失</li>
 *   <li>无限制增长导致 OOM</li>
 * </ol>
 * <p>
 * 设计：
 * <ul>
 *   <li>每个 memoryId 对应一个 Redis key，存储序列化后的消息列表</li>
 *   <li>最多保留 100 条消息，超出后截断保留最近的消息</li>
 *   <li>30 天 TTL —— 30 天未活跃的对话自动过期</li>
 *   <li>Redis 不可用时不崩溃，返回空列表降级</li>
 * </ul>
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
@Slf4j
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "chat:memory:";
    private static final int MAX_MESSAGES = 100;
    private static final Duration TTL = Duration.ofDays(30);

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + memoryId);
            if (json == null || json.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
        } catch (JsonProcessingException e) {
            log.warn("反序列化聊天记录失败 memoryId={}, 将返回空列表", memoryId, e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.warn("Redis 读取聊天记录失败 memoryId={}, 降级返回空列表", memoryId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            // 截断保留最近 MAX_MESSAGES 条，防止无限膨胀
            List<ChatMessage> trimmed = messages.size() > MAX_MESSAGES
                    ? new ArrayList<>(messages.subList(messages.size() - MAX_MESSAGES, messages.size()))
                    : messages;

            String json = objectMapper.writeValueAsString(trimmed);
            redis.opsForValue().set(KEY_PREFIX + memoryId, json, TTL);
        } catch (Exception e) {
            log.error("Redis 保存聊天记录失败 memoryId={}", memoryId, e);
            // 不抛出 —— 记忆丢失优于请求失败
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        try {
            redis.delete(KEY_PREFIX + memoryId);
        } catch (Exception e) {
            log.warn("Redis 删除聊天记录失败 memoryId={}", memoryId, e);
        }
    }
}
