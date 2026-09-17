package com.example.lipsticks.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.lipsticks.user.entity.UserAccount;
import com.example.lipsticks.user.mapper.UserAccountMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员角色一致性校验拦截器。
 * <p>
 * 补偿 JWT 无状态方案的固有问题：当管理员降级用户或封禁账号后，
 * 被降级/封禁用户持有的旧 Token（仍在有效期内）中携带的 role 不会自动更新。
 * <p>
 * 本拦截器仅拦截 /api/admin/** 路径，在每次管理操作前：
 * <ol>
 *   <li>从 MySQL 查询用户的当前 enabled 状态和 role</li>
 *   <li>与 JWT Token 中携带的 role 对比</li>
 *   <li>如果不一致 → 403 Forbidden，提示用户重新登录</li>
 * </ol>
 * <p>
 * 为什么只拦截 /api/admin/**？
 * <ul>
 *   <li>管理端是唯一的高风险面——非管理端可访问的数据（商品、推荐、色板）本身即为公开数据</li>
 *   <li>以最小代价覆盖最大风险</li>
 *   <li>查库走 uk_username 唯一索引，&lt;1ms</li>
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminRoleConsistencyInterceptor implements HandlerInterceptor {

    private final UserAccountMapper userAccountMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"请先登录\",\"data\":null}"
            );
            return false;
        }

        String username = auth.getName();

        // 只查 enabled 和 role —— 走 uk_username 唯一索引，极快
        UserAccount account = userAccountMapper.selectOne(
                Wrappers.<UserAccount>lambdaQuery()
                        .select(UserAccount::getEnabled, UserAccount::getRole)
                        .eq(UserAccount::getUsername, username)
        );

        if (account == null || Boolean.FALSE.equals(account.getEnabled())) {
            log.warn("管理端请求被拒绝: username={}, reason=账号不存在或已禁用", username);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"账号已被禁用\",\"data\":null,\"errorCode\":\"AUTH_1003\"}"
            );
            return false;
        }

        // 角色不一致检查 —— Token 中是 ADMIN，DB 中已降级为 USER
        String tokenRole = auth.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_USER");
        String dbRole = "ROLE_" + account.getRole();

        if (!tokenRole.equals(dbRole)) {
            log.warn("管理端请求被拒绝: username={}, tokenRole={}, dbRole={}", username, tokenRole, dbRole);
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"权限已变更，请重新登录\",\"data\":null,\"errorCode\":\"AUTH_1003\"}"
            );
            return false;
        }

        return true;
    }
}
