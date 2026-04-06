package com.xiao.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OAuth2 Token 传递过滤器
 * 将用户的 JWT Token 传递给下游服务
 * 
 * 注意：此类不使用 @Component 注解，而是通过 FilterConfig 手动注册，
 * 以便精确控制过滤器顺序和依赖注入
 */
public class OAuth2TokenRelayFilter implements GlobalFilter, Ordered {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public OAuth2TokenRelayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 尝试获取 OAuth2AuthenticationToken
        return exchange.getPrincipal()
            .cast(OAuth2AuthenticationToken.class)
            .flatMap(oauth2Token -> {
                String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

                // 使用 authorizedClientRepository 加载已授权的客户端
                return authorizedClientRepository.loadAuthorizedClient(registrationId, oauth2Token, exchange)
                    .flatMap(authorizedClient -> {
                        if (authorizedClient == null) {
                            return chain.filter(exchange);
                        }

                        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                        if (accessToken != null) {
                            String tokenValue = accessToken.getTokenValue();
                            // 将 Token 添加到请求头，传递给下游服务
                            ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(builder -> builder.header(HttpHeaders.AUTHORIZATION,
                                    "Bearer " + tokenValue))
                                .build();
                            return chain.filter(mutatedExchange);
                        }
                        return chain.filter(exchange);
                    });
            })
            // 如果没有 OAuth2AuthenticationToken 或发生错误，继续请求
            .onErrorResume(e -> chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之后执行，确保 SecurityContext 已经设置
        return 0;
    }
}
