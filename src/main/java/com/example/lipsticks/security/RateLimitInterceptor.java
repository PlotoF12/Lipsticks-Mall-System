package com.example.lipsticks.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AI 推荐接口限流拦截器。
 * <p>
 * 设计决策：
 * <ul>
 *   <li>放在 HandlerInterceptor 层而非 Security Filter Chain —— 限流是业务防护，不是安全控制</li>
 *   <li>登录用户按 username 限流，匿名用户按 IP 限流 —— 公平且精准</li>
 *   <li>使用 Redis ZSET 滑动窗口 —— 任何 60 秒窗口内严格不超过限额</li>
 *   <li>Lua 脚本保证原子性 —— 避免竞态条件导致的计数偏差</li>
 * </ul>
 * <p>
 * 返回 HTTP 429 + JSON body + Retry-After 头 —— 客户端无需解析 body 即可识别限流。
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "rate:ai:";
    private static final int ANONYMOUS_LIMIT = 5;   // 匿名用户 5次/分钟
    private static final int USER_LIMIT = 20;        // 登录用户 20次/分钟
    private static final int WINDOW_SECONDS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String identity = resolveIdentity(request);
        int limit = identity.startsWith("user:") ? USER_LIMIT : ANONYMOUS_LIMIT;

        String key = KEY_PREFIX + identity;
        long now = System.currentTimeMillis() / 1000;
        long windowStart = now - WINDOW_SECONDS;

        String lua = """
            redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1])
            local count = redis.call('ZCARD', KEYS[1])
            if count >= tonumber(ARGV[3]) then
                return -1
            end
            redis.call('ZADD', KEYS[1], ARGV[2], ARGV[2] .. '-' .. count)
            redis.call('EXPIRE', KEYS[1], ARGV[4])
            return tonumber(ARGV[3]) - count - 1
            """;

        Long remaining = redis.execute(
                new DefaultRedisScript<>(lua, Long.class),
                List.of(key),
                String.valueOf(windowStart),
                String.valueOf(now),
                String.valueOf(limit),
                String.valueOf(WINDOW_SECONDS + 1)
        );

        if (remaining == null || remaining < 0) {
            writeRateLimitResponse(response, key);
            return false;
        }

        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        return true;
    }

    private void writeRateLimitResponse(HttpServletResponse response, String key) {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");

        long retryAfter = 30;
        try {
            Set<String> timestamps = redis.opsForZSet().range(key, -1, -1);
            if (timestamps != null && !timestamps.isEmpty()) {
                long oldest = (long) Double.parseDouble(timestamps.iterator().next());
                retryAfter = Math.max(1, WINDOW_SECONDS
                        - (System.currentTimeMillis() / 1000 - oldest));
            }
        } catch (Exception ignored) {
        }

        response.setHeader("Retry-After", String.valueOf(retryAfter));

        try {
            Map<String, Object> body = Map.of(
                    "success", false,
                    "message", "请求过于频繁，请 " + retryAfter + " 秒后再试",
                    "data", null,
                    "errorCode", "RATE_6001",
                    "retryAfterSeconds", retryAfter
            );
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException ignored) {
        }
    }

    private String resolveIdentity(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof String username
                && !"anonymousUser".equals(username)) {
            return "user:" + username;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return "ip:" + ip;
    }
}
