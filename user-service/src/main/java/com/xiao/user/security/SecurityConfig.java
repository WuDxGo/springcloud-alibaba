package com.xiao.user.security;

import com.xiao.common.constant.AuthConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 用户服务安全配置
 *
 * <p> 作用：配置用户服务的Spring Security
 * <p> 特点：无状态、无CSRF、添加自定义过滤器
 *
 * @author: WuDx
 * @Date: 2026/2/28 10:55
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 10:55] [WuDx] 创建类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用CSRF
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 白名单放行
                        .requestMatchers(AuthConstant.WHITE_LIST).permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // 添加自定义过滤器（在UsernamePasswordAuthenticationFilter之前）
                .addFilterBefore(new UserAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}