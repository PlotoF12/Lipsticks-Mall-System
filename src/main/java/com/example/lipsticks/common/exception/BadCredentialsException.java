package com.example.lipsticks.common.exception;

/**
 * 认证失败异常 —— 用户名或密码错误。
 */
public class BadCredentialsException extends BusinessException {
    public BadCredentialsException() {
        super(ErrorCode.BAD_CREDENTIALS);
    }
}
