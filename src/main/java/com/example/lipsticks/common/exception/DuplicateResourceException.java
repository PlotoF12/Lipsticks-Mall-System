package com.example.lipsticks.common.exception;

/**
 * 重复资源异常 —— 如用户名已存在等。
 */
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
