package com.xiao.gateway.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证相关接口
 */
@RestController
public class AuthController {

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/api/auth/me")
    public Mono<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
        return oauth2User.map(user -> {
            Map<String, Object> info = new HashMap<>();
            info.put("username", user.getName());
            info.put("attributes", user.getAttributes());
            return info;
        }).defaultIfEmpty(new HashMap<>());
    }

    /**
     * 登出接口
     */
    @PostMapping("/api/auth/logout")
    public Mono<Void> logout() {
        return Mono.empty();
    }
}
