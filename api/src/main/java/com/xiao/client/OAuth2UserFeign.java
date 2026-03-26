package com.xiao.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * OAuth2 SSO 用户信息 Feign 客户端
 */
@FeignClient(name = "oauth-server", url = "http://localhost:8080")
public interface OAuth2UserFeign {

    /**
     * 获取当前登录用户信息
     * @param accessToken JWT Token
     * @return 用户信息
     */
    @GetMapping("/api/users/me")
    Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String accessToken);
}
