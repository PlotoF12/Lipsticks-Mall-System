package com.example.lipsticks.order.dto;

import lombok.Data;

/**
 * 模拟支付回调请求
 */
@Data
public class PaymentCallbackRequest {
    private String orderNo;
    private String paymentNo;
    /** SUCCESS / FAIL */
    private String result;
}
