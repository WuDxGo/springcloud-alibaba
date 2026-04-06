package com.xiao.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * OAuth2 资源服务器自动配置类
 * 
 * <h3>功能说明：</h3>
 * <ul>
 *   <li>自动配置 OAuth2 资源服务器，支持 JWT Token 验证</li>
 *   <li>提供基于方法的安全控制（@PreAuthorize、@Secured 等）</li>
 *   <li>支持自定义放行路径和权限前缀配置</li>
 * </ul>
 * 
 * <h3>生效条件：</h3>
 * <ul>
 *   <li>类路径存在 Spring Security 相关类</li>
 *   <li>未自定义 SecurityFilterChain Bean</li>
 *   <li>配置属性 common.security.enabled=true（默认 true）</li>
 * </ul>
 * 
 * <h3>自定义覆盖：</h3>
 * <p>如需自定义安全配置，只需在应用中声明自己的 SecurityFilterChain Bean，
 * 此自动配置将因 @ConditionalOnMissingBean 条件不满足而失效。</p>
 * 
 * <h3>配置示例：</h3>
 * <pre>
 * # application.yml
 * common:
 *   security:
 *     enabled: true  # 是否启用自动配置，默认 true
 *     permit-all-patterns:  # 放行路径列表
 *       - /actuator/**
 *       - /public/**
 *     authority-prefix: ROLE_  # 权限前缀
 *     authorities-claim-name: authorities  # JWT 中权限字段名
 * </pre>
 *
 * @author xiao
 */
@Configuration
@ConditionalOnClass({ EnableWebSecurity.class, OAuth2ResourceServerProperties.class })
@ConditionalOnMissingBean(SecurityFilterChain.class)
@ConditionalOnProperty(prefix = "common.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SecurityProperties.class)
@EnableWebSecurity
@EnableMethodSecurity
public class AutoSecurityConfig {

    private final SecurityProperties properties;

    public AutoSecurityConfig(SecurityProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（API 服务）
            .csrf(AbstractHttpConfigurer::disable)
            // 配置会话管理为无状态
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // 配置授权规则
            .authorizeHttpRequests(authorize -> {
                // 配置放行的路径
                for (String pattern : properties.getPermitAllPatterns()) {
                    authorize.requestMatchers(pattern).permitAll();
                }
                // 其他请求需要认证
                authorize.anyRequest().authenticated();
            })
            // 启用 OAuth2 资源服务器（JWT 验证）
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(properties.getAuthorityPrefix());
        grantedAuthoritiesConverter.setAuthoritiesClaimName(properties.getAuthoritiesClaimName());

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
