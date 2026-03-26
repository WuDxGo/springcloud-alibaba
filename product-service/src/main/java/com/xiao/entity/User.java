package com.xiao.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户表
 *
 * <p> 额外说明
 *
 * @author: WuDx
 * @Date: 2026/2/25 17:15
 * @Version [1.0]
 * @Version [1.0] [2026/2/25 17:15] [WuDx] 创建类
 */
@Setter
@Getter
public class User {
    private String id;

    private String userName;

    private String password;
}
