package com.xiao.gateway.config;

import com.xiao.gateway.filter.OAuth2TokenRelayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;

/**
 * Gateway 过滤器配置
 * 
 * 注意：OAuth2TokenRelayFilter 不使用 @Component 注解，
 * 此处通过手动注册方式精确控制过滤器顺序和依赖注入
 */
@Configuration
public class FilterConfig {

    @Bean
    @Order(1000)
    public GlobalFilter oauth2TokenRelayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        return new OAuth2TokenRelayFilter(authorizedClientRepository);
    }
}
