package com.xiao.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证过滤器
 *
 * <p> 作用：解析网关传递的用户信息，构建Spring Security上下文
 * <p> 特点：OncePerRequestFilter保证每个请求只执行一次
 *
 * @author: WuDx
 * @Date: 2026/2/28 10:53
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 10:53] [WuDx] 创建类
 */
@Slf4j
public class UserAuthFilter extends OncePerRequestFilter {

    /**
     * 内部过滤逻辑
     * 作用：从请求头中提取用户信息，构建Authentication对象
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {

        // 从请求头获取用户信息（由网关传递）
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String roles = request.getHeader("X-User-Roles");

        // 如果存在用户信息，构建认证对象
        if (userId != null && userName != null) {
            log.debug("用户已认证: userId={}, userName={}", userId, userName);

            // 解析角色
            List<SimpleGrantedAuthority> authorities = null;
            if (roles != null && !roles.isEmpty()) {
                authorities = Arrays.stream(roles.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // 创建认证令牌（已认证状态）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userName, null, authorities);

            // 设置到SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("未认证的请求");
        }

        // 继续过滤器链
        chain.doFilter(request, response);
    }
}
