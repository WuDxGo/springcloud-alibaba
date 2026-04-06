package com.xiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 用户实体类
 * 
 * <p>对应数据库表：user</p>
 *
 * @author: WuDx
 * @Date: 2026/2/25 17:15
 * @Version [1.0]
 * @Version [1.0] [2026/2/25 17:15] [WuDx] 创建类
 */
@Setter
@Getter
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    /**
     * 用户名
     */
    @TableField("user_name")
    private String userName;

    /**
     * 密码
     */
    @TableField("password")
    private String password;
}
