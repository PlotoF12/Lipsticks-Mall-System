package com.example.lipsticks.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lipsticks.common.exception.BusinessException;
import com.example.lipsticks.common.exception.ErrorCode;
import com.example.lipsticks.mall.entity.LipstickProduct;
import com.example.lipsticks.mall.mapper.LipstickProductMapper;
import com.example.lipsticks.order.dto.CreateOrderRequest;
import com.example.lipsticks.order.dto.OrderResponse;
import com.example.lipsticks.order.dto.OrderStatisticsResponse;
import com.example.lipsticks.order.entity.OrderInfo;
import com.example.lipsticks.order.entity.OrderItem;
import com.example.lipsticks.order.entity.PaymentRecord;
import com.example.lipsticks.order.mapper.OrderInfoMapper;
import com.example.lipsticks.order.mapper.OrderItemMapper;
import com.example.lipsticks.order.mapper.PaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final LipstickProductMapper lipstickProductMapper;
    private final PaymentService paymentService;

    /**
     * 创建订单并生成支付
     */
    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        // 1. 收集商品信息并计算金额
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            LipstickProduct product = lipstickProductMapper.selectById(itemReq.getProductId());
            if (product == null || Boolean.FALSE.equals(product.getOnSale())) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "商品 [" + itemReq.getProductId() + "] 不存在或已下架");
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK,
                        "商品 [" + product.getTitle() + "] 库存不足，当前库存: " + product.getStock());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductTitle(product.getTitle());
            item.setProductImage(product.getImageUrl());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice());
            orderItems.add(item);

            totalAmount += product.getPrice() * itemReq.getQuantity();
        }

        // 2. 生成订单号
        String orderNo = generateOrderNo();

        // 3. 创建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(orderNo);
        order.setUsername(username);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setRemark(request.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderInfoMapper.insert(order);

        // 4. 保存订单项
        for (OrderItem item : orderItems) {
            item.setOrderNo(orderNo);
            orderItemMapper.insert(item);
        }

        // 5. 扣减库存
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            LipstickProduct product = lipstickProductMapper.selectById(itemReq.getProductId());
            product.setStock(product.getStock() - itemReq.getQuantity());
            lipstickProductMapper.updateById(product);
        }

        // 6. 生成支付
        PaymentRecord payment = paymentService.createPayment(order, request.getPayMethod());

        log.info("[Order] 订单创建成功: orderNo={}, username={}, totalAmount={}, payMethod={}",
                orderNo, username, totalAmount, request.getPayMethod());

        return buildOrderResponse(order, orderItems, payment);
    }

    /**
     * 用户查询自己的订单列表（分页）
     */
    public Page<OrderResponse> listUserOrders(String username, int page, int size, String status) {
        LambdaQueryWrapper<OrderInfo> wrapper = Wrappers.<OrderInfo>lambdaQuery()
                .eq(OrderInfo::getUsername, username);
        if (status != null && !status.isBlank()) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        wrapper.orderByDesc(OrderInfo::getCreatedAt);

        Page<OrderInfo> orderPage = orderInfoMapper.selectPage(new Page<>(page, size), wrapper);

        Page<OrderResponse> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderResponse> responses = orderPage.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectList(
                            Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, order.getOrderNo()));
                    PaymentRecord payment = paymentRecordMapper.selectOne(
                            Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, order.getOrderNo()));
                    return buildOrderResponse(order, items, payment);
                })
                .collect(Collectors.toList());
        resultPage.setRecords(responses);
        return resultPage;
    }

    /**
     * 查询单个订单详情（用户只能看自己的）
     */
    public OrderResponse getOrderDetail(String username, String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
                Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!username.equals(order.getUsername())) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, orderNo));
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
        return buildOrderResponse(order, items, payment);
    }

    /**
     * 管理员查询任意订单详情
     */
    public OrderResponse getOrderDetailForAdmin(String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
                Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, orderNo));
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
        return buildOrderResponse(order, items, payment);
    }

    /**
     * 用户已购买的商品列表（已支付订单中的商品，去重）
     */
    public List<LipstickProduct> getPurchasedProducts(String username) {
        // 1. 查询用户所有已支付/已完成的订单号
        List<String> orderNos = orderInfoMapper.selectList(
                Wrappers.<OrderInfo>lambdaQuery()
                        .eq(OrderInfo::getUsername, username)
                        .in(OrderInfo::getStatus, List.of("PAID", "COMPLETED"))
                        .select(OrderInfo::getOrderNo)
        ).stream().map(OrderInfo::getOrderNo).toList();

        if (orderNos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查询这些订单中的商品ID（去重）
        List<Long> productIds = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .in(OrderItem::getOrderNo, orderNos)
                        .select(OrderItem::getProductId)
        ).stream().map(OrderItem::getProductId).distinct().toList();

        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 查询商品详情
        return lipstickProductMapper.selectBatchIds(productIds).stream()
                .filter(p -> Boolean.TRUE.equals(p.getOnSale()))
                .collect(Collectors.toList());
    }

    // ==================== 管理端 ====================

    /**
     * 管理员查询所有订单（分页）
     */
    public Page<OrderResponse> listAllOrders(int page, int size, String status, String username) {
        LambdaQueryWrapper<OrderInfo> wrapper = Wrappers.<OrderInfo>lambdaQuery();
        if (status != null && !status.isBlank()) {
            wrapper.eq(OrderInfo::getStatus, status);
        }
        if (username != null && !username.isBlank()) {
            wrapper.eq(OrderInfo::getUsername, username);
        }
        wrapper.orderByDesc(OrderInfo::getCreatedAt);

        Page<OrderInfo> orderPage = orderInfoMapper.selectPage(new Page<>(page, size), wrapper);

        Page<OrderResponse> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderResponse> responses = orderPage.getRecords().stream()
                .map(order -> {
                    List<OrderItem> items = orderItemMapper.selectList(
                            Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, order.getOrderNo()));
                    PaymentRecord payment = paymentRecordMapper.selectOne(
                            Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, order.getOrderNo()));
                    return buildOrderResponse(order, items, payment);
                })
                .collect(Collectors.toList());
        resultPage.setRecords(responses);
        return resultPage;
    }

    /**
     * 管理员更新订单状态
     */
    @Transactional
    public OrderResponse updateOrderStatus(String orderNo, String newStatus) {
        OrderInfo order = orderInfoMapper.selectOne(
                Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        Set<String> validStatuses = Set.of("PENDING_PAYMENT", "PAID", "COMPLETED", "CANCELLED");
        if (!validStatuses.contains(newStatus)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "无效的订单状态: " + newStatus + "，有效状态: " + validStatuses);
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderInfoMapper.updateById(order);

        if ("CANCELLED".equals(newStatus)) {
            // 取消订单时恢复库存
            List<OrderItem> items = orderItemMapper.selectList(
                    Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, orderNo));
            for (OrderItem item : items) {
                LipstickProduct product = lipstickProductMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    lipstickProductMapper.updateById(product);
                }
            }
        }

        List<OrderItem> items = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderNo, orderNo));
        PaymentRecord payment = paymentRecordMapper.selectOne(
                Wrappers.<PaymentRecord>lambdaQuery().eq(PaymentRecord::getOrderNo, orderNo));
        return buildOrderResponse(order, items, payment);
    }

    /**
     * 管理员获取订单统计
     */
    public OrderStatisticsResponse getStatistics() {
        List<OrderInfo> allOrders = orderInfoMapper.selectList(null);

        long totalOrders = allOrders.size();
        long paidOrders = allOrders.stream().filter(o -> "PAID".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus())).count();
        long pendingOrders = allOrders.stream().filter(o -> "PENDING_PAYMENT".equals(o.getStatus())).count();
        long completedOrders = allOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count();
        long cancelledOrders = allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count();

        // 总营收 = 已支付订单金额之和
        long totalRevenue = allOrders.stream()
                .filter(o -> "PAID".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus()))
                .mapToLong(OrderInfo::getTotalAmount)
                .sum();

        // 各支付方式占比
        List<PaymentRecord> allPayments = paymentRecordMapper.selectList(null);
        Map<String, Long> payMethodBreakdown = allPayments.stream()
                .filter(p -> "PAID".equals(p.getStatus()))
                .collect(Collectors.groupingBy(PaymentRecord::getPayMethod, Collectors.counting()));

        // 最近7天每日统计
        LocalDate today = LocalDate.now();
        List<OrderStatisticsResponse.DailyStats> dailyStats = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<OrderInfo> dayOrders = allOrders.stream()
                    .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().equals(date))
                    .toList();
            long count = dayOrders.size();
            long revenue = dayOrders.stream()
                    .filter(o -> "PAID".equals(o.getStatus()) || "COMPLETED".equals(o.getStatus()))
                    .mapToLong(OrderInfo::getTotalAmount)
                    .sum();
            dailyStats.add(OrderStatisticsResponse.DailyStats.builder()
                    .date(dateStr)
                    .orderCount(count)
                    .revenue(revenue)
                    .build());
        }

        return OrderStatisticsResponse.builder()
                .totalOrders(totalOrders)
                .paidOrders(paidOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .payMethodBreakdown(payMethodBreakdown)
                .dailyStats(dailyStats)
                .build();
    }

    // ==================== 内部方法 ====================

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "ORD" + timestamp + random;
    }

    private OrderResponse buildOrderResponse(OrderInfo order, List<OrderItem> items, PaymentRecord payment) {
        List<OrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productTitle(item.getProductTitle())
                        .productImage(item.getProductImage())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .username(order.getUsername())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .remark(order.getRemark())
                .payMethod(payment != null ? payment.getPayMethod() : null)
                .paymentStatus(payment != null ? payment.getStatus() : null)
                .qrCodeUrl(payment != null ? payment.getQrCodeUrl() : null)
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

}
