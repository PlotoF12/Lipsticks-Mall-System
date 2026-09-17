package com.example.lipsticks.recommend.service;

import com.example.lipsticks.common.exception.AiRateLimitException;
import com.example.lipsticks.common.exception.AiServiceException;
import com.example.lipsticks.common.exception.AiTimeoutException;
import com.example.lipsticks.common.exception.BusinessException;
import com.example.lipsticks.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * AI 推荐调用服务 —— 封装 LangChain4j 调用，提供分级重试机制。
 * <p>
 * 重试策略：
 * <ul>
 *   <li>429 (限流)：指数退避 1s → 2s → 4s，最多重试 3 次</li>
 *   <li>超时：指数退避 1s → 2s，最多重试 2 次</li>
 *   <li>连接错误：固定退避 1s，最多重试 1 次</li>
 *   <li>不可重试错误（401/400/413）：不重试，直接抛异常</li>
 * </ul>
 * <p>
 * 为什么不是所有错误都重试？<br>
 * 瞬时错误（限流、超时、连接中断）重试可能成功；<br>
 * 确定性错误（API Key 错误、Prompt 过长）重试多少次都不会改变结果。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiRecommendService {

    private final AiRecommendAssistant assistant;

    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_MS = 1000;

    /**
     * 带指数退避重试的 AI 推荐调用。
     *
     * @param memoryId 记忆ID
     * @param systemPrompt 动态 SystemMessage（含优先级规则、冲突提示）
     * @param catalog 商品目录文本
     * @param preferenceContext 偏好上下文（含优先级标注）
     * @param message 用户原始消息
     * @return AI 回复文本
     */
    public String chatWithRetry(String memoryId, String systemPrompt,
                                 String catalog, String preferenceContext,
                                 String message) {
        int attempt = 0;

        while (true) {
            try {
                return assistant.chatExtended(memoryId, systemPrompt, catalog, preferenceContext, message);

            } catch (RuntimeException e) {
                attempt++;

                RetryDecision decision = classify(e, attempt);

                if (!decision.shouldRetry) {
                    // 不可重试——直接抛给全局异常处理器
                    if (e instanceof BusinessException be) {
                        throw be;
                    }
                    throw new AiServiceException(ErrorCode.AI_ERROR, attempt, e);
                }

                log.warn("AI 调用失败 (attempt={}/{}), 原因={}, 将在 {}ms 后重试",
                        attempt, MAX_RETRIES,
                        e.getClass().getSimpleName(),
                        decision.backoffMs);

                try {
                    Thread.sleep(decision.backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AiServiceException(ErrorCode.AI_ERROR, attempt, e);
                }
            }
        }
    }

    /**
     * 异常分类器 —— 判断异常是否可重试以及重试策略。
     */
    private RetryDecision classify(RuntimeException e, int attempt) {
        String message = e.getMessage() != null ? e.getMessage() : "";
        Throwable cause = e.getCause();

        // === 遍历 cause chain 检查限流错误 ===
        Throwable current = e;
        while (current != null) {
            String msg = current.getMessage() != null ? current.getMessage() : "";
            if (msg.contains("429") || msg.contains("rate") || msg.contains("too many")) {
                return new RetryDecision(
                        true,
                        attempt <= 3,
                        INITIAL_BACKOFF_MS * (int) Math.pow(2, attempt - 1)  // 1s → 2s → 4s
                );
            }
            current = current.getCause();
        }

        // === 超时错误 ===
        if (message.contains("timeout") || message.contains("timed out")
                || (cause != null && cause instanceof SocketTimeoutException)) {
            return new RetryDecision(
                    true,
                    attempt <= 2,
                    INITIAL_BACKOFF_MS * (int) Math.pow(2, attempt - 1)  // 1s → 2s
            );
        }

        // === 连接错误 ===
        if (message.contains("connect") || message.contains("refused")
                || (cause != null && cause instanceof ConnectException)) {
            return new RetryDecision(true, attempt <= 1, INITIAL_BACKOFF_MS);
        }

        // === 不可重试 ===
        return new RetryDecision(false, false, 0);
    }

    /**
     * 重试决策。
     */
    private record RetryDecision(boolean isRetriable, boolean shouldRetry, long backoffMs) {
    }
}
