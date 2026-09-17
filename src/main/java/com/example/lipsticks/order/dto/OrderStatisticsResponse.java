package com.example.lipsticks.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderStatisticsResponse {
    /** 订单总数 */
    private long totalOrders;
    /** 已支付订单数 */
    private long paidOrders;
    /** 待支付订单数 */
    private long pendingOrders;
    /** 已完成订单数 */
    private long completedOrders;
    /** 已取消订单数 */
    private long cancelledOrders;
    /** 总营收（分） */
    private long totalRevenue;
    /** 各支付方式占比 */
    private Map<String, Long> payMethodBreakdown;
    /** 最近7天每日订单数 */
    private List<DailyStats> dailyStats;

    @Data
    @Builder
    public static class DailyStats {
        private String date;
        private long orderCount;
        private long revenue;
    }
}
