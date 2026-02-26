package com.xiao.entity;

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
public class User {
    private String id;

    private String userName;

    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
