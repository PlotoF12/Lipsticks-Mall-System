package com.example.lipsticks.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 短期意图与长期偏好之间的冲突记录。
 * <p>
 * 包含冲突的类型、描述、处理策略和具体指引，
 * 这些信息会被注入到 LLM 的 SystemMessage 中，
 * 让 LLM 按标注策略执行而非自行判断。
 */
@Data
@AllArgsConstructor
public class Conflict {

    /** 冲突类型: COLOR_CONTRADICTION / EXPLORE_VS_HABIT */
    private String type;

    /** 冲突描述，供人类阅读 */
    private String description;

    /** 处理策略: CURRENT_EXPLICIT_REQUEST（以当前要求为准）等 */
    private String resolutionStrategy;

    /** 具体的行为指引，注入到 SystemMessage 中指挥 LLM */
    private String guidance;
}
