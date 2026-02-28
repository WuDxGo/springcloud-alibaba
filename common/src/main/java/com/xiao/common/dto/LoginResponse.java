package com.xiao.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应DTO
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:35
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:35] [WuDx] 创建类
 */
@Data  // Lombok注解：自动生成getter、setter、toString等方法
@Builder  // Lombok注解：实现建造者模式，可以通过builder()方法链式创建对象
public class LoginResponse {

    /**
     * JWT令牌 - 用于后续请求的认证
     */
    private String token;

    /**
     * 刷新令牌（可选）- 用于获取新的访问令牌
     */
    private String refreshToken;

    /**
     * 令牌类型 - 通常是"Bearer"
     */
    private String tokenType;

    /**
     * 过期时间（秒）- 令牌的有效期
     */
    private Long expiresIn;

    /**
     * 用户基本信息 - 登录成功后返回的用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户基本信息内部类
     * 包含不敏感的用户信息
     */
    @Data
    @Builder
    public static class UserInfo {

        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 头像URL
         */
        private String avatar;
    }
}