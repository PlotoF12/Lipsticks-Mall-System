package com.example.lipsticks.common.exception;

/**
 * AI 服务超时异常。
 */
public class AiTimeoutException extends BusinessException {
    public AiTimeoutException(int retriesAttempted) {
        super(ErrorCode.AI_TIMEOUT, "已重试 " + retriesAttempted + " 次后超时");
    }
}
