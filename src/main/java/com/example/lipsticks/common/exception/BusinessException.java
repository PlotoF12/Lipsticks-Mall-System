package com.example.lipsticks.common.exception;

import lombok.Getter;

/**
 * 业务异常根类 —— 所有自定义业务异常从此继承。
 * <p>
 * 携带 ErrorCode 和可选的 detail 字段，
 * 由 {@link GlobalExceptionHandler} 统一拦截并转换为标准 API 响应。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
