package com.xiao.auth.security;

import com.xiao.common.constant.AuthConstant;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 6.x 配置类
 * 负责认证服务器的安全配置
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 用户详情服务，用于加载用户信息
     */
    private final UserDetailsService userDetailsService;

    /**
     * 认证配置，用于获取AuthenticationManager
     */
    private final AuthenticationConfiguration authenticationConfiguration;

    /**
     * 配置Security过滤链
     * 使用Spring Security 6.x新API，Lambda表达式配置
     *
     * @param http HttpSecurity对象，用于构建安全配置
     * @return SecurityFilterChain 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. 禁用CSRF保护（使用JWT不需要，因为JWT是无状态的）
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 设置会话管理为无状态（不使用Session）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. 配置请求授权
                .authorizeHttpRequests(auth -> auth
                        // 白名单路径允许所有人访问（不需要认证）
                        .requestMatchers(AuthConstant.WHITE_LIST).permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )

                // 4. 设置用户详情服务
                .userDetailsService(userDetailsService)

                // 5. 构建SecurityFilterChain
                .build();
    }

    /**
     * 认证管理器
     * 通过AuthenticationConfiguration获取，Spring Boot会自动配置
     *
     * @return AuthenticationManager 认证管理器
     * @throws Exception 获取异常
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}