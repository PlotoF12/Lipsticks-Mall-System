package com.example.lipsticks.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productImage;
    private Integer quantity;
    private Integer unitPrice;
}
