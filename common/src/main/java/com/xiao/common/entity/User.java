package com.xiao.common.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户实体类 - 通用用户信息
 * 用于在各个服务之间传递用户数据
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 15:22
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 15:22] [WuDx] 创建类
 */
@Data
public class User implements Serializable {
    /**
     * 序列化版本号
     * 用于确保序列化和反序列化时的版本兼容性
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     * 唯一标识一个用户
     */
    private Long id;

    /**
     * 用户名
     * 登录时使用，必须唯一
     */
    private String username;

    /**
     * 密码
     * BCrypt加密后的密文
     */
    private String password;

    /**
     * 手机号
     * 可用于登录和找回密码
     */
    private String phone;

    /**
     * 邮箱
     * 可用于登录和接收通知
     */
    private String email;

    /**
     * 头像URL
     * 用户头像的存储路径
     */
    private String avatar;

    /**
     * 用户状态
     * 0: 禁用, 1: 启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 角色集合
     * 用户拥有的所有角色，如：ADMIN, USER
     */
    private Set<String> roles;

    /**
     * 权限集合
     * 用户拥有的所有具体权限，如：user:view, order:create
     */
    private Set<String> permissions;
}
