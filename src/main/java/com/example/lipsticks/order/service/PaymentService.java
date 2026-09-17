package com.example.lipsticks.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.common.exception.BusinessException;
import com.example.lipsticks.common.exception.ErrorCode;
import com.example.lipsticks.order.entity.OrderInfo;
import com.example.lipsticks.order.entity.PaymentRecord;
import com.example.lipsticks.order.mapper.OrderInfoMapper;
import com.example.lipsticks.order.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final List<PaymentStrategy> paymentStrategies;
    private final PaymentRecordMapper paymentRecordMapper;
    private final OrderInfoMapper orderInfoMapper;

    /**
     * 根据支付方式选择合适的策略创建支付
     */
    public PaymentRecord createPayment(OrderInfo order, String payMethod) {
        PaymentStrategy strategy = paymentStrategies.stream()
                .filter(s -> s.supports(payMethod))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED,
                        "不支持的支付方式: " + payMethod));
        return strategy.createPayment(order);
    }

    /**
     * 模拟支付成功（实际应由支付回调触发）
     */
    @Transactional
    public PaymentRecord simulatePay(String orderNo, boolean success) {
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PROCESSED,
                    "该支付已处理，当前状态: " + payment.getStatus());
        }

        OrderInfo order = orderInfoMapper.selectOne(
                Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (success) {
            payment.setStatus("PAID");
            payment.setGatewayTradeNo("GTN" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
            payment.setGatewayResponse("{\"result\":\"SUCCESS\",\"msg\":\"支付成功\"}");
            payment.setPaidAt(LocalDateTime.now());
            order.setStatus("PAID");
            order.setUpdatedAt(LocalDateTime.now());
            log.info("[Payment] 支付成功: orderNo={}, paymentNo={}, amount={}", orderNo, payment.getPaymentNo(), payment.getAmount());
        } else {
            payment.setStatus("FAILED");
            payment.setGatewayResponse("{\"result\":\"FAIL\",\"msg\":\"模拟支付失败\"}");
            log.info("[Payment] 支付失败: orderNo={}, paymentNo={}", orderNo, payment.getPaymentNo());
        }

        paymentRecordMapper.updateById(payment);
        orderInfoMapper.updateById(order);

        return payment;
    }

    /**
     * 支付回调处理（扩展点 —— 接入真实支付宝/微信回调时使用）
     */
    @Transactional
    public PaymentRecord handlePaymentCallback(String orderNo, String gatewayTradeNo, String payMethod, String result) {
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
        if (payment == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        boolean success = "SUCCESS".equalsIgnoreCase(result);
        if (success) {
            payment.setStatus("PAID");
            payment.setGatewayTradeNo(gatewayTradeNo);
            payment.setGatewayResponse("{\"callback_result\":\"SUCCESS\"}");
            payment.setPaidAt(LocalDateTime.now());

            OrderInfo order = orderInfoMapper.selectOne(
                    Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
            if (order != null) {
                order.setStatus("PAID");
                order.setUpdatedAt(LocalDateTime.now());
                orderInfoMapper.updateById(order);
            }
        } else {
            payment.setStatus("FAILED");
            payment.setGatewayResponse("{\"callback_result\":\"FAIL\"}");
        }

        paymentRecordMapper.updateById(payment);
        return payment;
    }

    /**
     * 查询支付记录
     */
    public PaymentRecord getPaymentByOrderNo(String orderNo) {
        return paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
    }
}
