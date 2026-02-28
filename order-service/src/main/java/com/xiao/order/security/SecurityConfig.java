package com.xiao.order.security;

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
 * 订单服务安全配置
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/28 11:15
 * @Version [1.0]
 * @Version [1.0] [2026/2/28 11:15] [WuDx] 创建类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AuthConstant.WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new OrderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}