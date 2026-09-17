package com.example.lipsticks.order.service;

import com.example.lipsticks.order.entity.OrderInfo;
import com.example.lipsticks.order.entity.PaymentRecord;

/**
 * 支付策略接口 —— 定义统一支付流程。
 * 支持支付宝和微信支付两种实现，便于后续扩展。
 */
public interface PaymentStrategy {

    /**
     * 判断是否支持该支付方式
     */
    boolean supports(String payMethod);

    /**
     * 生成支付 —— 创建支付记录，返回支付信息（含二维码链接等）
     */
    PaymentRecord createPayment(OrderInfo order);

    /**
     * 查询支付状态
     */
    String queryPaymentStatus(PaymentRecord record);

    /**
     * 获取支付方式名称
     */
    String getPayMethod();
}
