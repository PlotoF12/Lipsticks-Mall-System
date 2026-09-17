package com.example.lipsticks.common.exception;

/**
 * 资源不存在异常 —— 如商品已下架、用户不存在等。
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
