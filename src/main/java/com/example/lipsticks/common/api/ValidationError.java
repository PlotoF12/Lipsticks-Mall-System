package com.example.lipsticks.common.api;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 字段级校验错误信息。
 */
@Data
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}
