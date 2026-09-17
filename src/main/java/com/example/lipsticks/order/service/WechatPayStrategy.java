package com.example.lipsticks.order.service;

import com.example.lipsticks.order.entity.OrderInfo;
import com.example.lipsticks.order.entity.PaymentRecord;
import com.example.lipsticks.order.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 微信支付策略 —— 模拟实现。
 * <p>
 * 实际生产环境替换为微信支付 SDK（wechatpay-java）调用。
 * 当前使用模拟数据，完整保留了回调签名验证等扩展点。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatPayStrategy implements PaymentStrategy {

    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    public boolean supports(String payMethod) {
        return "WECHAT".equalsIgnoreCase(payMethod);
    }

    @Override
    public PaymentRecord createPayment(OrderInfo order) {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase());
        record.setOrderNo(order.getOrderNo());
        record.setPayMethod("WECHAT");
        record.setAmount(order.getTotalAmount());
        record.setStatus("PENDING");
        // 模拟微信支付扫码链接
        record.setQrCodeUrl("wechat://pay/qrcode?orderNo=" + order.getOrderNo() + "&amount=" + order.getTotalAmount());
        record.setCreatedAt(LocalDateTime.now());
        paymentRecordMapper.insert(record);
        log.info("[WechatPay] 创建支付记录: paymentNo={}, orderNo={}, amount={}", record.getPaymentNo(), order.getOrderNo(), order.getTotalAmount());
        return record;
    }

    @Override
    public String queryPaymentStatus(PaymentRecord record) {
        // 模拟：实际应调用微信支付查询接口
        return record.getStatus();
    }

    @Override
    public String getPayMethod() {
        return "WECHAT";
    }
}
