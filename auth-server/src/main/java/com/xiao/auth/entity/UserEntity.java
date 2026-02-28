package com.xiao.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类（数据库映射）
 * 对应数据库中的user表
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/27 16:52
 * @Version [1.0]
 * @Version [1.0] [2026/2/27 16:52] [WuDx] 创建类
 */
@Data
@TableName("user")  // MyBatis Plus：指定数据库表名
public class UserEntity {

    /**
     * 用户ID
     * 主键，自增长
     */
    @TableId(type = IdType.AUTO)  // 主键策略：数据库自增
    private Long id;

    /**
     * 用户名
     * 唯一，用于登录
     */
    private String username;

    /**
     * 密码
     * BCrypt加密后的密文
     */
    private String password;

    /**
     * 手机号
     * 可用于登录
     */
    private String phone;

    /**
     * 邮箱
     * 可用于登录和找回密码
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户状态
     * 0-禁用，1-启用
     * status是MySQL关键字，需要转义
     */
    @TableField("`status`")  // 指定字段名并转义
    private Integer status;

    /**
     * 创建时间
     * 插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 插入和更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时自动填充
    private LocalDateTime updateTime;
}