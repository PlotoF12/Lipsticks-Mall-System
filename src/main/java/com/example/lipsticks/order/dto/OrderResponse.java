package com.example.lipsticks.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNo;
    private String username;
    private Integer totalAmount;
    private String status;
    private String remark;
    private String payMethod;
    private String paymentStatus;
    private String qrCodeUrl;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productTitle;
        private String productImage;
        private Integer quantity;
        private Integer unitPrice;
    }
}
