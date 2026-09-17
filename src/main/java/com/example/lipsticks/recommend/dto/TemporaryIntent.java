package com.example.lipsticks.recommend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 从短期对话窗口中提取的临时意图。
 * <p>
 * 区分于存储在 MySQL 中的长期偏好（UserPreference），
 * TemporaryIntent 仅反映用户在当前或最近几轮对话中表达的意图。
 */
@Data
public class TemporaryIntent {

    /** 用户是否表达了探索新风格的意愿 */
    private boolean exploreNew = false;

    /** 用户明确要求的色系列表 */
    private List<String> requestedColors = new ArrayList<>();

    /** 用户提到的场景 */
    private String scene;

    /** 用户是否有明确的排除声明（"不要XX"） */
    private boolean hasExplicitExclusion = false;

    public boolean hasContent() {
        return exploreNew
                || !requestedColors.isEmpty()
                || scene != null
                || hasExplicitExclusion;
    }
}
