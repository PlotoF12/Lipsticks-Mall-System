package com.example.lipsticks.common.exception;

/**
 * 账号已被禁用异常。
 */
public class AccountDisabledException extends BusinessException {
    public AccountDisabledException() {
        super(ErrorCode.ACCOUNT_DISABLED);
    }
}
