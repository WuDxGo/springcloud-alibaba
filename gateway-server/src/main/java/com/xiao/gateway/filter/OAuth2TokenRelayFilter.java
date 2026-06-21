package com.xiao.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OAuth2 Token 传递过滤器
 * 将用户的 JWT Token 传递给下游服务
 *
 * 注意：此类不使用 @Component 注解，而是通过 FilterConfig 手动注册，
 * 以便精确控制过滤器顺序和依赖注入
 */
@Slf4j
public class OAuth2TokenRelayFilter implements GlobalFilter, Ordered {

    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;

    public OAuth2TokenRelayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
            .flatMap(principal -> {
                if (principal instanceof JwtAuthenticationToken jwtToken) {
                    return chain.filter(withBearerToken(exchange, jwtToken.getToken().getTokenValue()));
                }

                if (principal instanceof OAuth2AuthenticationToken oauth2Token) {
                    String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

                    return authorizedClientRepository.loadAuthorizedClient(registrationId, oauth2Token, exchange)
                        .flatMap(authorizedClient -> {
                            if (authorizedClient == null) {
                                log.debug("未找到 OAuth2 授权客户端: {}", registrationId);
                                return chain.filter(exchange);
                            }

                            OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                            if (accessToken != null) {
                                log.debug("已传递 OAuth2 Token 到下游服务");
                                return chain.filter(withBearerToken(exchange, accessToken.getTokenValue()));
                            }
                            log.debug("OAuth2 AccessToken 为空，跳过传递");
                            return chain.filter(exchange);
                        });
                }

                return chain.filter(exchange);
            })
            .switchIfEmpty(chain.filter(exchange))
            .onErrorResume(e -> {
                log.warn("OAuth2 Token 传递失败，继续请求: {}", e.getMessage());
                return chain.filter(exchange);
            });
    }

    private ServerWebExchange withBearerToken(ServerWebExchange exchange, String tokenValue) {
        return exchange.mutate()
            .request(builder -> builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValue))
            .build();
    }

    @Override
    public int getOrder() {
        // 在认证过滤器之后执行，确保 SecurityContext 已经设置
        return 0;
    }
}
