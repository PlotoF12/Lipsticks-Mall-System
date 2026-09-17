package com.example.lipsticks.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemRequest> items;

    private String remark;

    @NotNull(message = "支付方式不能为空")
    private String payMethod;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品ID不能为空")
        private Long productId;
        @NotNull(message = "购买数量不能为空")
        private Integer quantity;
    }
}
