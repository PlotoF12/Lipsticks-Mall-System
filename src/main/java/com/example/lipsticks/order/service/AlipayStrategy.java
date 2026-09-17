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
 * 支付宝支付策略 —— 模拟实现。
 * <p>
 * 实际生产环境替换为支付宝 SDK（alipay-sdk-java）调用。
 * 当前使用模拟数据，完整保留了回调签名验证等扩展点。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayStrategy implements PaymentStrategy {

    private final PaymentRecordMapper paymentRecordMapper;

    @Override
    public boolean supports(String payMethod) {
        return "ALIPAY".equalsIgnoreCase(payMethod);
    }

    @Override
    public PaymentRecord createPayment(OrderInfo order) {
        PaymentRecord record = new PaymentRecord();
        record.setPaymentNo("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase());
        record.setOrderNo(order.getOrderNo());
        record.setPayMethod("ALIPAY");
        record.setAmount(order.getTotalAmount());
        record.setStatus("PENDING");
        // 模拟支付宝扫码支付链接
        record.setQrCodeUrl("alipay://qr/pay?orderNo=" + order.getOrderNo() + "&amount=" + order.getTotalAmount());
        record.setCreatedAt(LocalDateTime.now());
        paymentRecordMapper.insert(record);
        log.info("[Alipay] 创建支付记录: paymentNo={}, orderNo={}, amount={}", record.getPaymentNo(), order.getOrderNo(), order.getTotalAmount());
        return record;
    }

    @Override
    public String queryPaymentStatus(PaymentRecord record) {
        // 模拟：实际应调用支付宝查询接口
        return record.getStatus();
    }

    @Override
    public String getPayMethod() {
        return "ALIPAY";
    }
}
