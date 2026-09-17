package com.example.lipsticks.recommend.dto;

import lombok.Data;

@Data
public class AiChatRequest {
    private String message;
    /**
     * 用于多轮对话记忆的会话ID（未登录用户也可用）。
     * 登录用户会优先使用 username 作为 memoryId。
     */
    private String conversationId;
}
