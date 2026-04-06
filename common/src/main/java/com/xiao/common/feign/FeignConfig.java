package com.xiao.common.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置类
 * 注册 Token 拦截器，自动将 JWT Token 添加到 Feign 请求头
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignTokenInterceptor() {
        return new FeignTokenInterceptor();
    }
}
