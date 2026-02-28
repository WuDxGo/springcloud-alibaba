package com.xiao.order.security;

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
 * 订单认证过滤器
 *
 * <p> 作用：解析网关传递的用户信息，构建Spring Security上下文
 * <p> 与UserAuthFilter类似，为订单服务提供认证支持
 *
 * @author: WuDx
 * @Date: 2026/2/28 11:14
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 11:14] [WuDx] 创建类
 */
@Slf4j
public class OrderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {

        // 从请求头获取用户信息
        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String roles = request.getHeader("X-User-Roles");

        if (userId != null && userName != null) {
            log.debug("用户已认证: userId={}, userName={}", userId, userName);

            // 解析角色
            List<SimpleGrantedAuthority> authorities = null;
            if (roles != null && !roles.isEmpty()) {
                authorities = Arrays.stream(roles.split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            // 创建认证令牌
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userName, null, authorities);

            // 设置到SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.debug("未认证的请求");
        }

        chain.doFilter(request, response);
    }
}
