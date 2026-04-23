package com.example.lipsticks.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lipstick_product")
public class LipstickProduct {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String brand;
    private String shade;
    private String colorHex;
    private String category;
    private String finishType;
    private String detail;
    private Integer price;
    private Integer stock;
    private Boolean onSale;
    private String suitableSkinTone;
    private String suitableGender;
    private String scene;
    private String imageUrl;
}
