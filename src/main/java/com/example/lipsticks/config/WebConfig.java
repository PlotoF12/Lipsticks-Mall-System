package com.example.lipsticks.config;

import com.example.lipsticks.security.AdminRoleConsistencyInterceptor;
import com.example.lipsticks.security.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置 —— 注册拦截器。
 * <p>
 * 拦截器执行顺序（按注册顺序）：
 * <ol>
 *   <li>AdminRoleConsistencyInterceptor —— 管理端角色一致性校验</li>
 *   <li>RateLimitInterceptor —— AI 推荐接口限流（需 Redis 可用）</li>
 * </ol>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminRoleConsistencyInterceptor adminRoleConsistencyInterceptor;

    @Autowired(required = false)
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理端角色一致性 —— 补偿 JWT 无状态方案，实时校验 DB 中的角色
        registry.addInterceptor(adminRoleConsistencyInterceptor)
                .addPathPatterns("/api/admin/**")
                .order(1);

        // AI 推荐限流 —— 仅在 Redis 可用时启用
        if (rateLimitInterceptor != null) {
            registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/api/recommend/ai")
                    .order(2);
        }
    }
}
