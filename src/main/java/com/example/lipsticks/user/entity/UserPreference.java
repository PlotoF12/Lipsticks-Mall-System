package com.example.lipsticks.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户长期偏好表 —— 从对话历史中异步提取的结构化偏好数据。
 * <p>
 * 与 user_profile 的区别：
 * <ul>
 *   <li>user_profile 是用户手动填写的画像（gender/skinTone/skinType）</li>
 *   <li>user_preference 是系统从对话中自动学习的偏好（色系/质地/品牌/场景等）</li>
 * </ul>
 */
@Data
@TableName("user_preference")
public class UserPreference {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;

    /** 偏好的色系，逗号分隔: "nude,bean_paste" */
    private String preferredColors;

    /** 偏好的质地: matte / gloss / satin */
    private String preferredFinish;

    /** 偏好的品牌，逗号分隔: "Dior,MAC" */
    private String preferredBrands;

    /** 价格区间下限 */
    private Integer priceMin;

    /** 价格区间上限 */
    private Integer priceMax;

    /** 不喜欢的色系，逗号分隔: "purple,bright_pink" */
    private String dislikedColors;

    /** 常用场景，逗号分隔: "日常,通勤" */
    private String scenes;

    /** 唇部状况: dry / normal */
    private String lipCondition;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
