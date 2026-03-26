package com.xiao.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

/**
 * Gateway 安全配置
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            // 完全禁用 CSRF（WebFlux OAuth2 登录需要）
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // 配置授权规则
            .authorizeExchange(exchanges -> exchanges
                // 放行 OAuth2 相关端点（必须在最前面）
                .pathMatchers("/oauth2/**").permitAll()
                .pathMatchers("/login/**").permitAll()
                .pathMatchers("/logout").permitAll()
                // 放行错误页面
                .pathMatchers("/error").permitAll()
                // 放行静态资源
                .pathMatchers("/static/**", "/public/**", "/favicon.ico").permitAll()
                // 放行 Actuator 端点
                .pathMatchers("/actuator/**").permitAll()
                // 放行 API 文档
                .pathMatchers("/api/auth/**").permitAll()
                // 其他请求需要认证
                .anyExchange().authenticated()
            )
            // 启用 OAuth2 登录
            .oauth2Login(oauth2 -> oauth2
                .authorizedClientRepository(authorizedClientRepository())
            )
            // 启用 OAuth2 资源服务器（JWT 验证）
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter()))
                )
            )
            // 配置登出
            .logout(logout -> logout
                .logoutUrl("/logout")
            )
            // 配置异常处理 - 未认证时重定向到 OAuth2 登录
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((exchange, e) -> {
                    // 重定向到 OAuth2 授权端点
                    String redirectUrl = "/oauth2/authorization/gateway-client";
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().set("Location", redirectUrl);
                    return exchange.getResponse().setComplete();
                })
            );

        // 允许 iframe 嵌入（开发环境）
        http.headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions
                .mode(XFrameOptionsServerHttpHeadersWriter.Mode.SAMEORIGIN)
            )
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * 配置登录错误页面路由
     */
    @Bean
    public RouterFunction<ServerResponse> loginErrorRoute() {
        return RouterFunctions.route(GET("/login"), request -> {
            String error = request.queryParam("error").orElse(null);
            if (error != null) {
                // 返回错误页面
                return ServerResponse.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .bodyValue("<html><body><h1>登录失败</h1><p>错误：" + error + "</p><a href='/oauth2/authorization/gateway-client'>重新登录</a></body></html>");
            }
            return ServerResponse.notFound().build();
        });
    }
}
