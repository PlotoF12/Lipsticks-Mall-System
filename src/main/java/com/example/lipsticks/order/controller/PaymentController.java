package com.example.lipsticks.order.controller;

import com.example.lipsticks.common.api.ApiResponse;
import com.example.lipsticks.order.entity.PaymentRecord;
import com.example.lipsticks.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 模拟支付（生产环境应移除，仅用于开发测试）
     * POST /api/payment/simulate
     * Body: { "orderNo": "ORD...", "success": true }
     */
    @PostMapping("/simulate")
    public ApiResponse<Map<String, Object>> simulatePay(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        Boolean success = (Boolean) body.getOrDefault("success", true);

        PaymentRecord payment = paymentService.simulatePay(orderNo, success);

        return ApiResponse.ok(Map.of(
                "orderNo", payment.getOrderNo(),
                "paymentNo", payment.getPaymentNo(),
                "status", payment.getStatus(),
                "message", "PAID".equals(payment.getStatus()) ? "支付成功" : "支付失败"
        ));
    }

    /**
     * 查询支付状态
     */
    @GetMapping("/status/{orderNo}")
    public ApiResponse<Map<String, String>> queryPaymentStatus(@PathVariable String orderNo) {
        PaymentRecord payment = paymentService.getPaymentByOrderNo(orderNo);
        if (payment == null) {
            return ApiResponse.fail("支付记录不存在");
        }
        return ApiResponse.ok(Map.of(
                "orderNo", payment.getOrderNo(),
                "paymentNo", payment.getPaymentNo(),
                "payMethod", payment.getPayMethod(),
                "status", payment.getStatus(),
                "qrCodeUrl", payment.getQrCodeUrl() != null ? payment.getQrCodeUrl() : ""
        ));
    }
}
