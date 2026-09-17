package com.example.lipsticks.common.exception;

/**
 * AI 服务被限流异常 —— DeepSeek 返回 429。
 */
public class AiRateLimitException extends BusinessException {
    public AiRateLimitException(int retriesAttempted) {
        super(ErrorCode.AI_RATE_LIMITED, "已重试 " + retriesAttempted + " 次后被限流");
    }
}
