package com.example.lipsticks.common.exception;

/**
 * AI 服务异常（不可重试的错误）。
 */
public class AiServiceException extends BusinessException {
    public AiServiceException(ErrorCode errorCode, int retriesAttempted, Throwable cause) {
        super(errorCode, "已重试 " + retriesAttempted + " 次后仍失败", cause);
    }
}
