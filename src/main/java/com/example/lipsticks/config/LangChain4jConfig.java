package com.example.lipsticks.config;

import com.example.lipsticks.recommend.service.AiRecommendAssistant;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {

    /**
     * 配置LangChain4j的AI服务
     */
    @Bean
    public AiRecommendAssistant aiRecommendAssistant(ChatLanguageModel chatLanguageModel) {
        return AiServices.create(AiRecommendAssistant.class, chatLanguageModel);
    }
}
