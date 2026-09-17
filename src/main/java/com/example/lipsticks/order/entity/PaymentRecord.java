package com.example.lipsticks.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String paymentNo;
    private String orderNo;
    private String payMethod;
    private Integer amount;
    private String status;
    private String qrCodeUrl;
    private String gatewayTradeNo;
    private String gatewayResponse;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
