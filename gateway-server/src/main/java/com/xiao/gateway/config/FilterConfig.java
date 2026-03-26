package com.xiao.gateway.config;

import com.xiao.gateway.filter.OAuth2TokenRelayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;

/**
 * Gateway 过滤器配置
 */
@Configuration
public class FilterConfig {

    @Bean
    @Order(1000)
    public GlobalFilter oauth2TokenRelayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        return new OAuth2TokenRelayFilter(authorizedClientRepository);
    }
}
